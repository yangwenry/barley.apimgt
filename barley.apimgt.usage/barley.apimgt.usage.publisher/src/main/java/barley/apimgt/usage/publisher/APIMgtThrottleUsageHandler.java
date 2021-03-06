/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package barley.apimgt.usage.publisher;

import barley.apimgt.gateway.APIMgtGatewayConstants;
import barley.apimgt.gateway.handlers.security.APISecurityUtils;
import barley.apimgt.gateway.handlers.security.AuthenticationContext;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.usage.publisher.dto.ThrottlePublisherDTO;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;

/*
* This is the class mediator which will handle publishing events upon throttle out events
*/
public class APIMgtThrottleUsageHandler extends APIMgtCommonExecutionPublisher {


    public APIMgtThrottleUsageHandler() {
        super();
    }



    public boolean mediate(MessageContext messageContext) {
        super.mediate(messageContext);
        if (publisher == null) {
            this.initializeDataPublisher();
        }

        try {
            if (!enabled || skipEventReceiverConnection) {
                return true;
            }
            // gets the access token and username
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext
                    (messageContext);
            if (authContext != null) {
                long currentTime = System.currentTimeMillis();
                String throttleOutReason = APIConstants.THROTTLE_OUT_REASON_SOFT_LIMIT_EXCEEDED;

                if(messageContext.getProperty(APIConstants.THROTTLE_OUT_REASON_KEY) != null){
                    throttleOutReason = (String) messageContext.getProperty(APIConstants.THROTTLE_OUT_REASON_KEY);
                }

                String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
                String tenantDomain = getTenantDomain(messageContext);

                ThrottlePublisherDTO throttlePublisherDTO = new ThrottlePublisherDTO();
                throttlePublisherDTO.setAccessToken(authContext.getApiKey());
                String username = authContext.getUsername();
                throttlePublisherDTO.setUsername(username);
                throttlePublisherDTO.setTenantDomain(tenantDomain);
                throttlePublisherDTO.setApiname((String) messageContext.getProperty(
                        APIMgtGatewayConstants.API));
                throttlePublisherDTO.setApiVersion(apiVersion);
                throttlePublisherDTO.setVersion((String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
                throttlePublisherDTO.setContext((String) messageContext.getProperty(
                        APIMgtGatewayConstants.CONTEXT));
                throttlePublisherDTO.setProvider((String) messageContext.getProperty(
                        APIMgtGatewayConstants.API_PUBLISHER));
                throttlePublisherDTO.setApplicationName((String) messageContext.getProperty(
                        APIMgtGatewayConstants.APPLICATION_NAME));
                throttlePublisherDTO.setApplicationId((String) messageContext.getProperty(
                        APIMgtGatewayConstants.APPLICATION_ID));
                throttlePublisherDTO.setThrottledTime(currentTime);
                throttlePublisherDTO.setThrottledOutReason(throttleOutReason);
                throttlePublisherDTO.setSubscriber(authContext.getSubscriber());
                publisher.publishEvent(throttlePublisherDTO);


            }


        } catch (Exception e) {
            log.error("Cannot publish throttling event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware() {
        return false;
    }
}

