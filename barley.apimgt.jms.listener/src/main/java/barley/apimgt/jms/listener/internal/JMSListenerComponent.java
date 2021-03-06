/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package barley.apimgt.jms.listener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.Constants;
//import org.osgi.framework.InvalidSyntaxException;
//import org.osgi.framework.ServiceEvent;
//import org.osgi.framework.ServiceListener;
//import org.osgi.framework.ServiceReference;
//import org.osgi.framework.ServiceRegistration;
//import org.osgi.service.component.ComponentContext;

import barley.apimgt.gateway.service.APIThrottleDataService;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.APIManagerConfigurationService;
import barley.apimgt.jms.listener.JMSListenerShutDownService;
import barley.apimgt.jms.listener.utils.JMSTransportHandler;
import barley.apimgt.jms.listener.utils.ListenerConstants;


/**
 * This component start the JMS listeners
 */

/**
 * @scr.component name="org.wso2.apimgt.jms.listener" immediate="true"
 * @scr.reference name="throttle.data.service"
 * interface="org.wso2.carbon.apimgt.gateway.service.APIThrottleDataService"
 * cardinality="1..1" policy="dynamic" bind="setAPIThrottleDataService" unbind="unsetAPIThrottleDataService"
 * org.wso2.andes.wso2.service.QpidNotificationService
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */

//public class JMSListenerComponent implements ServiceListener {
public class JMSListenerComponent {

    private static final Log log = LogFactory.getLog(JMSListenerComponent.class);
    private JMSTransportHandler jmsTransportHandler;
    //private BundleContext bundleContext;
    //private ServiceRegistration registration;

    public void activate() {
        log.debug("Activating component...");

        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        if (configuration != null) {
            if (!configuration.getThrottleProperties().getJmsConnectionProperties().isEnabled()) {
                return;
            }

        } else {
            log.warn("API Manager Configuration not properly set.");
        }

//        BundleContext bundleContext = context.getBundleContext();
//        this.bundleContext = bundleContext;
        boolean andesBundlePresent = false;

        /* (??????)
        for (Bundle bundle : bundleContext.getBundles()) {
            if (ListenerConstants.ANDES_SYMBOLIC_NAME.equals(bundle.getSymbolicName())) {

                // Searching for the QpidNotificationService. This service is exposed by andes,
                // so if this service is exposed we know for sure andes has started properly.
                String notificationServiceFilter = String.format("(%s=%s)", Constants.OBJECTCLASS,
                                                                 ListenerConstants.QPID_SERVICE);
                andesBundlePresent = true;
                try {
                    ServiceReference[] serviceReferences =
                            bundleContext.getServiceReferences((String) null, notificationServiceFilter);
                    // If the service is present we'd directly start the listener.
                    if (serviceReferences != null) {
                        startJMSListener();
                    } else {
                        bundleContext.addServiceListener(this, notificationServiceFilter);
                    }
                    break;
                } catch (InvalidSyntaxException e) {
                    log.error("Error while querying " + ListenerConstants.QPID_SERVICE + " service", e);
                }
            }
        }
        */

        // If andes bundle is not in the environment, then listener should be configured to a jms publisher running
        // in a remote machine. That's why we start the listener.
        if (!andesBundlePresent) {
            startJMSListener();
        }

        return;
    }

    private void startJMSListener() {
        jmsTransportHandler = new JMSTransportHandler();
        jmsTransportHandler.subscribeForJmsEvents();
        /* (??????)
        if(bundleContext != null) {
            registration = bundleContext.registerService(
                    JMSListenerShutDownService.class.getName(),
                    new JMSListenerShutDownService() {

                        @Override
                        public void shutDownListener() {
                            // Shutting down the listener
                            log.debug("Shutdown service called... Starting to shutdown listener.");
                            jmsTransportHandler.unSubscribeFromEvents();
                        }
                    }, null);
        }
        */
    }


    public void setAPIThrottleDataService(APIThrottleDataService throttleDataService) {
        log.debug("Setting APIThrottleDataService");
        ServiceReferenceHolder.getInstance().setAPIThrottleDataService(throttleDataService);
    }

    public void unsetAPIThrottleDataService(APIThrottleDataService throttleDataService) {
        log.debug("Un-setting APIThrottleDataService");
        ServiceReferenceHolder.getInstance().setAPIThrottleDataService(null);
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    public void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    public void deactivate() {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating component");
        }
        /* (??????)
        if(this.registration != null){
            this.registration.unregister();
        } 
        */
        if(jmsTransportHandler != null){
            // This method will make shutdown the Listener.
            log.debug("Unsubscribing from JMS Events...");
            jmsTransportHandler.unSubscribeFromEvents();
        }
    }

    /*@Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            if (log.isDebugEnabled()) {
                log.debug("Service " + ListenerConstants.QPID_SERVICE + " fulfilled.");
            }
            startJMSListener();
        }
    }*/
}
