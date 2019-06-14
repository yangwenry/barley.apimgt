package barley.apimgt.gateway.internal;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.gateway.handlers.security.keys.APIKeyValidatorClientPool;
import barley.apimgt.gateway.handlers.security.thrift.ThriftKeyValidatorClientPool;
import barley.apimgt.gateway.service.APIThrottleDataService;
import barley.apimgt.gateway.service.APIThrottleDataServiceImpl;
import barley.apimgt.gateway.throttling.ThrottleDataHolder;
import barley.apimgt.gateway.throttling.util.BlockingConditionRetriever;
import barley.apimgt.gateway.throttling.util.KeyTemplateRetriever;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.APIManagerConfigurationService;
import barley.apimgt.impl.observers.TenantServiceCreator;
import barley.core.utils.Axis2ConfigurationContextObserver;
import barley.core.utils.BarleyUtils;

// gateway 구동시 서비스 초기화코드로 이 로직을 처리해놓았기 때문에 현재 사용하지 않는다. 
public class APIHandlerServiceComponent {

	private static final Log log = LogFactory.getLog(APIHandlerServiceComponent.class);

    private APIKeyValidatorClientPool clientPool;
    private ThriftKeyValidatorClientPool thriftClientPool;
    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    private ServiceRegistration registration;

    // 수정 
    public void activate() {
    	// BundleContext bundleContext = context.getBundleContext();
        if (log.isDebugEnabled()) {
            log.debug("API handlers component activated");
        }
        clientPool = APIKeyValidatorClientPool.getInstance();
        thriftClientPool = ThriftKeyValidatorClientPool.getInstance();

        String filePath = BarleyUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "api-manager.xml";
		try {
			configuration.load(filePath);

			String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
			if ("Synapse".equalsIgnoreCase(gatewayType)) {
				//Register Tenant service creator to deploy tenant specific common synapse configurations
				/* (주석) 
				TenantServiceCreator listener = new TenantServiceCreator();
			  	bundleContext.registerService(
			          Axis2ConfigurationContextObserver.class.getName(), listener, null);
				*/
				if (configuration.getThrottleProperties().isEnabled()) {
                    ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
                    APIThrottleDataServiceImpl throttleDataServiceImpl = new APIThrottleDataServiceImpl();
                    throttleDataServiceImpl.setThrottleDataHolder(throttleDataHolder);

                    // Register APIThrottleDataService so that ThrottleData maps are available to other components.
                    /*
                    registration = context.getBundleContext().registerService(
                            APIThrottleDataService.class.getName(),
                            throttleDataServiceImpl, null);
                            */
                    ServiceReferenceHolder.getInstance().setThrottleDataHolder(throttleDataHolder);

                    log.debug("APIThrottleDataService Registered...");
                    ServiceReferenceHolder.getInstance().setThrottleProperties(configuration
                            .getThrottleProperties());


                    //First do web service call and update map.
                    //Then init JMS listener to listen que and update it.
                    //Following method will initialize JMS listnet and listen all updates and keep throttle data map
                    // up to date
                    //start web service throttle data retriever as separate thread and start it.
                    if (configuration.getThrottleProperties().getBlockCondition().isEnabled()){
                        BlockingConditionRetriever webServiceThrottleDataRetriever = new
                                BlockingConditionRetriever();
                        webServiceThrottleDataRetriever.startWebServiceThrottleDataRetriever();
                        KeyTemplateRetriever webServiceBlockConditionsRetriever = new
                                KeyTemplateRetriever();
                        webServiceBlockConditionsRetriever.startKeyTemplateDataRetriever();
                    }
                }
            }
		} catch (APIManagementException e) {
			log.error("Error while initializing the API Gateway (APIHandlerServiceComponent) component", e);
		}
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("API handlers component deactivated");
        }
        clientPool.cleanup();
        thriftClientPool.cleanup();
        if(registration != null){
            log.debug("Unregistering ThrottleDataService...");
            registration.unregister();
        }
    }

    /* (주석)
    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("Configuration context service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(cfgCtxService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("Configuration context service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(null);
    }
    */

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }
}
