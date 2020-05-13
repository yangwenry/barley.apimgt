package barley.apimgt.usage.publisher;

import barley.apimgt.gateway.APIMgtGatewayConstants;
import barley.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;

public class APIMgtFaultHandler extends APIMgtCommonExecutionPublisher {


    public APIMgtFaultHandler() {
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
            // (수정) 2019.07.16 - REQUEST_START_TIME이 null을 리턴하므로 현재 시간으로 변경 
//            long requestTime = Long.parseLong((String) messageContext.getProperty(APIMgtGatewayConstants.
//                                                                         REQUEST_START_TIME));
            long requestTime = Long.parseLong((String) messageContext.getProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME));

            /*
            String apiPublisher = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
            String tenantDomain = null;
            if(apiPublisher != null) {
            	tenantDomain = MultitenantUtils.getTenantDomain(apiPublisher);
            }
            */
            String apiPublisher = getApiPublisher(messageContext);
            String tenantDomain = getTenantDomain(messageContext);

            FaultPublisherDTO faultPublisherDTO = new FaultPublisherDTO();
//            faultPublisherDTO.setConsumerKey((String) messageContext.getProperty(
//                    APIMgtGatewayConstants.CONSUMER_KEY));
            faultPublisherDTO.setContext((String) messageContext.getProperty(
                    APIMgtGatewayConstants.CONTEXT));
            faultPublisherDTO.setApiVersion((String) messageContext.getProperty(
                    APIMgtGatewayConstants.API_VERSION));
            faultPublisherDTO.setApi((String) messageContext.getProperty(
                    APIMgtGatewayConstants.API));
            faultPublisherDTO.setResourcePath((String) messageContext.getProperty(
                    APIMgtGatewayConstants.RESOURCE));
            faultPublisherDTO.setMethod((String) messageContext.getProperty(
                    APIMgtGatewayConstants.HTTP_METHOD));
            faultPublisherDTO.setVersion((String) messageContext.getProperty(
                    APIMgtGatewayConstants.VERSION));
            faultPublisherDTO.setErrorCode(String.valueOf(messageContext.getProperty(
                    SynapseConstants.ERROR_CODE)));
            faultPublisherDTO.setErrorMessage((String) messageContext.getProperty(
                    SynapseConstants.ERROR_MESSAGE));
            faultPublisherDTO.setRequestTime(requestTime);
//            faultPublisherDTO.setUsername((String) messageContext.getProperty(
//                    APIMgtGatewayConstants.USER_ID));
            // (수정) 2019.07.16 - Null이 발생하여 주석처리 
            /*
            faultPublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(
                    faultPublisherDTO.getUsername()));
                    */
            faultPublisherDTO.setTenantDomain(tenantDomain);
            
            faultPublisherDTO.setHostName((String) messageContext.getProperty(
                    APIMgtGatewayConstants.HOST_NAME));
            faultPublisherDTO.setApiPublisher(apiPublisher);
//            faultPublisherDTO.setApplicationName((String) messageContext.getProperty(
//                    APIMgtGatewayConstants.APPLICATION_NAME));
//            faultPublisherDTO.setApplicationId((String) messageContext.getProperty(
//                    APIMgtGatewayConstants.APPLICATION_ID));
            String protocol = (String) messageContext.getProperty(
                    SynapseConstants.TRANSPORT_IN_NAME);
            faultPublisherDTO.setProtocol(protocol);

            publisher.publishEvent(faultPublisherDTO);

        } catch (Exception e) {
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware() {
        return false;
    }
}
