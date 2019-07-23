/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package barley.apimgt.gateway.handlers.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.RESTDispatcher;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import barley.apimgt.gateway.APIMgtGatewayConstants;
import barley.apimgt.gateway.handlers.Utils;
import barley.apimgt.gateway.internal.ServiceReferenceHolder;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.utils.APIUtil;
import barley.core.MultitenantConstants;
import barley.core.multitenancy.MultitenantUtils;

import org.apache.axis2.Constants;


public class CORSRequestHandler extends AbstractHandler implements ManagedLifecycle {

	private static final Log log = LogFactory.getLog(CORSRequestHandler.class);
	private String apiImplementationType;
	private String allowHeaders;
	private String allowCredentials;
	private Set<String> allowedOrigins;
	private boolean initializeHeaderValues;
	private String allowedMethods;
	private boolean allowCredentialsEnabled;
	public void init(SynapseEnvironment synapseEnvironment) {
		if (log.isDebugEnabled()) {
			log.debug("Initializing CORSRequest Handler instance");
		}
		if (ServiceReferenceHolder.getInstance().getApiManagerConfigurationService() != null) {
			 initializeHeaders();
		}
	}

	/**
	 * This method used to Initialize  header values
	 *
	 * @return true after Initialize the values
	 */
	void initializeHeaders() {
		if (allowHeaders == null) {
			allowHeaders = APIUtil.getAllowedHeaders();
		}
		if (allowedOrigins == null) {
			String allowedOriginsList = APIUtil.getAllowedOrigins();
			if (!allowedOriginsList.isEmpty()) {
				allowedOrigins = new HashSet<String>(Arrays.asList(allowedOriginsList.split(",")));
			}
		}
		if (allowCredentials == null) {
			allowCredentialsEnabled = APIUtil.isAllowCredentials();
		}
		if (allowedMethods == null) {
			allowedMethods = APIUtil.getAllowedMethods();
		}

		initializeHeaderValues =  true;
	}

	public void destroy() {
		if (log.isDebugEnabled()) {
			log.debug("Destroying CORSRequest Handler instance");
		}
	}

	public boolean handleRequest(MessageContext messageContext) {
		long executionStartTime = System.currentTimeMillis();
		// (임시주석)
//		Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
//                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName()));
//        Timer.Context context = timer.start();

        try {
            if (!initializeHeaderValues) {
                initializeHeaders();
            }
            String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
			String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
			String httpMethod = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    getProperty(Constants.Configuration.HTTP_METHOD);
			API selectedApi = messageContext.getConfiguration().getAPI(apiName);
            Resource selectedResource = null;
			String subPath = null;
            String path = RESTUtils.getFullRequestPath(messageContext);
			if(selectedApi != null) {
				if (VersionStrategyFactory.TYPE_URL.equals(selectedApi.getVersionStrategy().getVersionType())) {
					subPath = path.substring(
							selectedApi.getContext().length() + selectedApi.getVersionStrategy().getVersion().length() + 1);
				} else {
					subPath = path.substring(selectedApi.getContext().length());
				}
			}
            if ("".equals(subPath)) {
                subPath = "/";
            }
            messageContext.setProperty(RESTConstants.REST_SUB_REQUEST_PATH, subPath);

            if(selectedApi != null){
                Resource[] allAPIResources = selectedApi.getResources();

				Set<Resource> acceptableResources = new HashSet<Resource>();

				for(Resource resource : allAPIResources){
					//If the requesting method is OPTIONS or if the Resource contains the requesting method
					if (RESTConstants.METHOD_OPTIONS.equals(httpMethod) ||
							(resource.getMethods() != null && Arrays.asList(resource.getMethods()).contains(httpMethod))) {
						acceptableResources.add(resource);
					}
				}

                if (acceptableResources.size() > 0) {
                    for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
                        Resource resource = dispatcher.findResource(messageContext, acceptableResources);
                        if (resource != null) {
                            selectedResource = resource;
                            break;
                        }
                    }
                }
				//If no acceptable resources are found
				else {
					//We're going to send a 405 or a 404. Run the following logic to determine which.
					handleResourceNotFound(messageContext, Arrays.asList(allAPIResources));
					return false;
				}

				//No matching resource found
				if(selectedResource == null) {
					//Respond with a 404
					onResourceNotFoundError(messageContext, HttpStatus.SC_NOT_FOUND,
							APIMgtGatewayConstants.RESOURCE_NOT_FOUND_ERROR_MSG);
					return false;
				}
            }

            String resourceString = selectedResource.getDispatcherHelper().getString();
            String resourceCacheKey = APIUtil
                    .getResourceInfoDTOCacheKey(apiContext, apiVersion, resourceString, httpMethod);
            messageContext.setProperty(APIConstants.API_ELECTED_RESOURCE, resourceString);
            messageContext.setProperty(APIConstants.API_RESOURCE_CACHE_KEY, resourceCacheKey);

			//If this is an OPTIONS request
			if (APIConstants.SupportedHTTPVerbs.OPTIONS.name().equalsIgnoreCase(httpMethod)) {
				//If the OPTIONS method is explicity specified in the resource
				if (Arrays.asList(selectedResource.getMethods()).contains(
						APIConstants.SupportedHTTPVerbs.OPTIONS.name())) {
					//We will not handle the CORS headers, let the back-end do it.
					return true;
				}
				setCORSHeaders(messageContext, selectedResource);
				Mediator corsSequence = messageContext.getSequence(APIConstants.CORS_SEQUENCE_NAME);
				if (corsSequence != null) {
					corsSequence.mediate(messageContext);
				}
				Utils.send(messageContext, HttpStatus.SC_OK);
				return false;
			}
			else if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(apiImplementationType)) {
				setCORSHeaders(messageContext, selectedResource);
				messageContext.getSequence(APIConstants.CORS_SEQUENCE_NAME).mediate(messageContext);
			}
			setCORSHeaders(messageContext, selectedResource);
			return true;
		} finally {
//            context.stop();
        }        
    }

	public boolean handleResponse(MessageContext messageContext) {
		Mediator corsSequence = messageContext.getSequence(APIConstants.CORS_SEQUENCE_NAME);
		if (corsSequence != null) {
			corsSequence.mediate(messageContext);
		}
		return true;
	}

	private void handleResourceNotFound(MessageContext messageContext, List<Resource> allAPIResources) {

		Resource uriMatchingResource = null;

		for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
			uriMatchingResource = dispatcher.findResource(messageContext, allAPIResources);
			//If a resource with a matching URI was found.
			if (uriMatchingResource != null) {
				onResourceNotFoundError(messageContext, HttpStatus.SC_METHOD_NOT_ALLOWED,
						APIMgtGatewayConstants.METHOD_NOT_FOUND_ERROR_MSG);
				return;
			}
		}

		//If a resource with a matching URI was not found.
		if(uriMatchingResource == null) {
			//Respond with a 404.
			onResourceNotFoundError(messageContext, HttpStatus.SC_NOT_FOUND,
					APIMgtGatewayConstants.RESOURCE_NOT_FOUND_ERROR_MSG);
			return;
		}
	}

	private void onResourceNotFoundError(MessageContext messageContext, int statusCode, String errorMessage) {
		messageContext.setProperty(APIConstants.CUSTOM_HTTP_STATUS_CODE, statusCode);
		messageContext.setProperty(APIConstants.CUSTOM_ERROR_CODE, statusCode);
		messageContext.setProperty(APIConstants.CUSTOM_ERROR_MESSAGE, errorMessage);
		Mediator resourceMisMatchedSequence = messageContext.getSequence(RESTConstants.NO_MATCHING_RESOURCE_HANDLER);
		if (resourceMisMatchedSequence != null) {
			// (추가) 2019.07.17 
			setAPIParametersToMessageContext(messageContext);
			resourceMisMatchedSequence.mediate(messageContext);
		}
	}
	
	// (추가) 2019.07.17 - wso2 소스는 아니지만 fault 실행시 Null데이터가 많아서 프로퍼티 추가를 위해 구현함   
	private void setAPIParametersToMessageContext(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String context = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);

        String apiPublisher = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);

        int index = apiName.indexOf("--");
        String apiVersion = apiName;
        if (index != -1) {
            apiVersion = apiName.substring(index + 2);
            if (apiPublisher == null) {
                apiPublisher = APIUtil.replaceEmailDomainBack(apiName.substring(0, index));
            }
        } 

        String api = apiVersion.split(":")[0];
        String version = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        String resource = extractResource(messageContext);
        String method = (String) (axis2MsgContext.getProperty(Constants.Configuration.HTTP_METHOD));
        String hostName = APIUtil.getHostAddress();

        messageContext.setProperty(APIMgtGatewayConstants.CONTEXT, context);
        messageContext.setProperty(APIMgtGatewayConstants.API_VERSION, apiVersion);
        messageContext.setProperty(APIMgtGatewayConstants.API, api);
        messageContext.setProperty(APIMgtGatewayConstants.VERSION, version);
        messageContext.setProperty(APIMgtGatewayConstants.RESOURCE, resource);
        messageContext.setProperty(APIMgtGatewayConstants.HTTP_METHOD, method);
        messageContext.setProperty(APIMgtGatewayConstants.HOST_NAME, hostName);
        messageContext.setProperty(APIMgtGatewayConstants.API_PUBLISHER, apiPublisher);
    }
	
	// (추가) 2019.07.17
	private String extractResource(MessageContext mc) {
        String resource = "/";
        Pattern pattern = Pattern.compile(APIMgtGatewayConstants.RESOURCE_PATTERN);
        Matcher matcher = pattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()) {
            resource = matcher.group(1);
        }
        return resource;
    }

	/**
	 * This method used to set CORS headers into message context
	 *
	 * @param messageContext   message context for set cors headers as properties
	 * @param selectedResource resource according to the request
	 */
	public void setCORSHeaders(MessageContext messageContext, Resource selectedResource) {
		org.apache.axis2.context.MessageContext axis2MC =
				((Axis2MessageContext) messageContext).getAxis2MessageContext();
		Map<String, String> headers =
				(Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		String requestOrigin = headers.get("Origin");
		String allowedOrigin = getAllowedOrigins(requestOrigin);

		//Set the access-Control-Allow-Credentials header in the response only if it is specified to true in the api-manager configuration
		//and the allowed origin is not the wildcard (*)
        if (allowCredentialsEnabled && !"*".equals(allowedOrigin)) {
            messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE);
        }

		messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
		String allowedMethods = "";
		StringBuffer allowedMethodsBuffer = new StringBuffer();
		if (selectedResource != null) {
			String[] methods = selectedResource.getMethods();
			for (String method : methods) {
				allowedMethodsBuffer.append(method).append(',');
				}
			allowedMethods = allowedMethodsBuffer.toString();
			if (methods.length != 0) {
				allowedMethods = allowedMethods.substring(0, allowedMethods.length() - 1);
			}
		} else {
			allowedMethods = this.allowedMethods;
		}
		if ("*".equals(allowHeaders)) {
			allowHeaders = headers.get("Access-Control-Request-Headers");

		}
		messageContext.setProperty(APIConstants.CORS_CONFIGURATION_ENABLED, APIUtil.isCORSEnabled());
		messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, allowedMethods);
		messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
	}


	public String getAllowHeaders() {
		return allowHeaders;
	}

	public void setAllowHeaders(String allowHeaders) {
		this.allowHeaders = allowHeaders;
	}

	public String getAllowedOrigins(String origin) {
		if (allowedOrigins.contains("*")) {
			return "*";
		} else if (allowedOrigins.contains(origin)) {
			return origin;
		} else {
			return null;
		}
	}

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = new HashSet<String>(Arrays.asList(allowedOrigins.split(",")));
    }

	public String getApiImplementationType() {
		return apiImplementationType;
	}

	public void setApiImplementationType(String apiImplementationType) {
		this.apiImplementationType = apiImplementationType;
	}

	// For backward compatibility with 1.9.0 since the property name is inline
	public String getInline() { return getApiImplementationType(); }

	// For backward compatibility with 1.9.0 since the property name is inline
	public void setInline(String inlineType) {
		setApiImplementationType(inlineType);
	}

	public String isAllowCredentials() {
		return allowCredentials;
	}

	public void setAllowCredentials(String allowCredentials) {
		this.allowCredentialsEnabled = Boolean.parseBoolean(allowCredentials);
		this.allowCredentials = allowCredentials;
	}

	public String getAllowedMethods() {
		return allowedMethods;
	}

	public void setAllowedMethods(String allowedMethods) {
		this.allowedMethods = allowedMethods;
	}
}
