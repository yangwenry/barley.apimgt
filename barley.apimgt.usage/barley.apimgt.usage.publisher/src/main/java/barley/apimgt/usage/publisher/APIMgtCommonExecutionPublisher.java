package barley.apimgt.usage.publisher;


import barley.apimgt.gateway.APIMgtGatewayConstants;
import barley.apimgt.gateway.dto.ExecutionTimePublisherDTO;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.usage.publisher.internal.ServiceReferenceHolder;
import barley.core.MultitenantConstants;
import barley.core.context.PrivilegedBarleyContext;
import barley.core.multitenancy.MultitenantUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;

public class APIMgtCommonExecutionPublisher extends AbstractMediator {
    protected boolean enabled;

    protected boolean skipEventReceiverConnection;

    protected volatile APIMgtUsageDataPublisher publisher;

    public APIMgtCommonExecutionPublisher() {
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            this.initializeDataPublisher();
        }

    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        if (enabled
                && !skipEventReceiverConnection) {
            Object totalTimeObject = messageContext.getProperty(APIMgtGatewayConstants
                    .REQUEST_EXECUTION_START_TIME);
            long totalTime = 0;
            if (totalTimeObject != null) {
                totalTime = Long.parseLong((String) totalTimeObject);
            }
            totalTime = System.currentTimeMillis() - totalTime;
            String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
            String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            /* (수정) 
            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(RESTUtils.getFullRequestPath
                    (messageContext));
            if(StringUtils.isEmpty(tenantDomain)){
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            String provider = APIUtil.getAPIProviderFromRESTAPI(apiName, tenantDomain);
            */
            String provider = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
            String tenantDomain = null;
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            if(provider != null) {
            	tenantDomain = MultitenantUtils.getTenantDomain(provider);
            	tenantId = APIUtil.getTenantId(provider);
            }
            
            ExecutionTimePublisherDTO executionTimePublisherDTO = new ExecutionTimePublisherDTO();
            executionTimePublisherDTO.setApiName(APIUtil.getAPINamefromRESTAPI(apiName));
            executionTimePublisherDTO.setVersion(apiVersion);
            executionTimePublisherDTO.setContext(apiContext);
            executionTimePublisherDTO.setTenantDomain(tenantDomain);
            executionTimePublisherDTO.setApiResponseTime(totalTime);
            executionTimePublisherDTO.setProvider(provider);
            executionTimePublisherDTO.setTenantId(tenantId);
            Object securityLatency = messageContext.getProperty(APIMgtGatewayConstants.SECURITY_LATENCY);
            executionTimePublisherDTO.setSecurityLatency(securityLatency == null ? 0 :
                                                          ((Number) securityLatency).longValue());
            Object throttleLatency =  messageContext.getProperty(APIMgtGatewayConstants.THROTTLING_LATENCY);
            executionTimePublisherDTO.setThrottlingLatency(throttleLatency == null ? 0 :
                                                          ((Number) throttleLatency).longValue());
            Object reqMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.REQUEST_MEDIATION_LATENCY);
            executionTimePublisherDTO.setRequestMediationLatency(reqMediationLatency == null ? 0 :
                    ((Number) reqMediationLatency).longValue());
            Object resMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_MEDIATION_LATENCY);
            executionTimePublisherDTO.setResponseMediationLatency(resMediationLatency == null ? 0 :
                    ((Number) resMediationLatency).longValue());
            Object otherLatency = messageContext.getProperty(APIMgtGatewayConstants.OTHER_LATENCY);
            executionTimePublisherDTO.setOtherLatency(otherLatency == null ? 0 :
                    ((Number) otherLatency).longValue());
            Object backendLatency = messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY);
            executionTimePublisherDTO.setBackEndLatency(backendLatency == null ? 0 :
                    ((Number) backendLatency).longValue());
            executionTimePublisherDTO.setEventTime(System.currentTimeMillis());
            publisher.publishEvent(executionTimePublisherDTO);

        }
        return true;
    }

    protected void initializeDataPublisher() {

        enabled = APIUtil.isAnalyticsEnabled();
        skipEventReceiverConnection = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();
        if (!enabled || skipEventReceiverConnection) {
            return;
        }
        if (publisher == null) {
            synchronized (this) {
                if (publisher == null) {
                    String publisherClass = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                            .getPublisherClass();
                    try {
                        log.debug("Instantiating Data Publisher");
                        PrivilegedBarleyContext.startTenantFlow();
                        PrivilegedBarleyContext.getThreadLocalCarbonContext().
                                setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        APIMgtUsageDataPublisher tempPublisher = (APIMgtUsageDataPublisher) APIUtil.getClassForName
                                (publisherClass).newInstance();
                        tempPublisher.init();
                        publisher = tempPublisher;
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + publisherClass, e);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + publisherClass, e);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + publisherClass, e);
                    } finally {
                    	PrivilegedBarleyContext.endTenantFlow();
                    }
                }
            }
        }
    }

    protected String getTenantDomain(MessageContext messageContext) {
        String apiPublisher = getApiPublisher(messageContext);
        String tenantDomain = MultitenantUtils.getTenantDomain(apiPublisher);
        return tenantDomain;
    }

    protected String getApiPublisher(MessageContext messageContext) {
        // CORSRequestHandler 소스 참조
        String apiPublisher = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        int index = apiName.indexOf("--");
        if (index != -1) {
            if (apiPublisher == null) {
                apiPublisher = APIUtil.replaceEmailDomainBack(apiName.substring(0, index));
            }
        }
        return apiPublisher;
    }
}
