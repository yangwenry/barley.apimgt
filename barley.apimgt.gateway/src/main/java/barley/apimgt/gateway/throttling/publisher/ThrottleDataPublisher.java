/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package barley.apimgt.gateway.throttling.publisher;


import barley.apimgt.gateway.handlers.security.AuthenticationContext;
import barley.apimgt.gateway.internal.ServiceReferenceHolder;
import barley.apimgt.impl.dto.ThrottleProperties;
import barley.databridge.agent.DataPublisher;
import barley.databridge.agent.exception.DataEndpointAgentConfigurationException;
import barley.databridge.agent.exception.DataEndpointAuthenticationException;
import barley.databridge.agent.exception.DataEndpointConfigurationException;
import barley.databridge.agent.exception.DataEndpointException;
import barley.databridge.commons.exception.TransportException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Throttle data publisher class is here to publish throttle data to global policy engine.
 * This can publish data according to defined protocol. Protocol can be thrift or binary.
 * When we use this for high concurrency usecases proper tuning is mandatory.
 */
public class ThrottleDataPublisher {
    public static ThrottleDataPublisherPool dataPublisherPool;

    public static final Log log = LogFactory.getLog(ThrottleDataPublisher.class);

    public static DataPublisher getDataPublisher() {
        return dataPublisher;
    }

    private static volatile DataPublisher dataPublisher = null;

    ThreadPoolExecutor executor;

    /**
     * This method will initialize throttle data publisher. Inside this we will start executor and initialize data
     * publisher which we used to publish throttle data.
     */
    public ThrottleDataPublisher() {
        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance().getThrottleProperties();
        if (throttleProperties != null) {
            dataPublisherPool = ThrottleDataPublisherPool.getInstance();
            ThrottleProperties.DataPublisher dataPublisherConfiguration = ServiceReferenceHolder.getInstance()
                    .getThrottleProperties().getDataPublisher();
            if (dataPublisherConfiguration.isEnabled()){
                ThrottleProperties.DataPublisherThreadPool dataPublisherThreadPoolConfiguration = ServiceReferenceHolder
                        .getInstance().getThrottleProperties().getDataPublisherThreadPool();

                try {
                    executor = new DataPublisherThreadPoolExecutor(dataPublisherThreadPoolConfiguration.getCorePoolSize(),
                            dataPublisherThreadPoolConfiguration.getMaximumPoolSize(), dataPublisherThreadPoolConfiguration
                            .getKeepAliveTime(),
                            TimeUnit
                                    .SECONDS,
                            new LinkedBlockingDeque<Runnable>() {
                            });
                    dataPublisher = new DataPublisher(dataPublisherConfiguration.getType(), dataPublisherConfiguration
                            .getReceiverUrlGroup(), dataPublisherConfiguration.getAuthUrlGroup(), dataPublisherConfiguration
                            .getUsername(),
                            dataPublisherConfiguration.getPassword());

                } catch (DataEndpointAgentConfigurationException e) {
                    log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                            e.getMessage(), e);
                } catch (DataEndpointException e) {
                    log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                            e.getMessage(), e);
                } catch (DataEndpointConfigurationException e) {
                    log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                            e.getMessage(), e);
                } catch (DataEndpointAuthenticationException e) {
                    log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                            e.getMessage(), e);
                } catch (TransportException e) {
                    log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                            e.getMessage(), e);
                }
            }
        }
    }

    /**
     * This method used to pass message context and let it run within separate thread.
     *
     * @param messageContext is message context object that holds
     */
    public void publishNonThrottledEvent(
            String applicationLevelThrottleKey, String applicationLevelTier,
            String apiLevelThrottleKey, String apiLevelTier,
            String subscriptionLevelThrottleKey, String subscriptionLevelTier,
            String resourceLevelThrottleKey, String resourceLevelTier,
            String authorizedUser, String apiContext, String apiVersion, String appTenant, String apiTenant,
            String appId, long throttleLimit,
            MessageContext messageContext,
            AuthenticationContext authenticationContext) {
        try {
            DataProcessAndPublishingAgent agent = dataPublisherPool.get();
            agent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                    apiLevelThrottleKey, apiLevelTier,
                    subscriptionLevelThrottleKey, subscriptionLevelTier,
                    resourceLevelThrottleKey, resourceLevelTier,
                    authorizedUser, apiContext, apiVersion, appTenant, apiTenant, appId, throttleLimit,
                    messageContext,
                    authenticationContext);
            executor.execute(agent);
        } catch (Exception e) {
            log.error("Error while publishing throttling events to global policy server", e);
        }
    }

    /**
     * This class will act as thread pool executor and after executing each thread it will return runnable
     * object back to pool. This implementation specifically used to minimize number of objectes created during
     * runtime. In this queuing strategy the submitted task will wait in the queue if the corePoolsize theads are
     * busy and the task will be allocated if any of the threads become idle.Thus ThreadPool will always have number
     * of threads running  as mentioned in the corePoolSize.
     * LinkedBlockingQueue without the capacity can be used for this queuing strategy.If the corePoolsize of the
     * threadpool is less and there are more number of time consuming task were submitted,there is more possibility
     * that the task has to wait in the queue for more time before it is run by any of the ideal thread.
     * So tuning core pool size is something we need to tune properly.
     * Also no task will be rejected in Threadpool until the threadpool was shutdown.
     */
    private class DataPublisherThreadPoolExecutor extends ThreadPoolExecutor {
        public DataPublisherThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                               TimeUnit unit, LinkedBlockingDeque<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        protected void afterExecute(java.lang.Runnable r, java.lang.Throwable t) {
            try {
                DataProcessAndPublishingAgent agent = (DataProcessAndPublishingAgent) r;
                //agent.setDataReference(null);
                ThrottleDataPublisher.dataPublisherPool.release(agent);
            } catch (Exception e) {
                log.error("Error while returning Throttle data publishing agent back to pool" + e.getMessage());
            }
        }
    }

    // (??????)
    public void destroy() {
    	/* dataPublisher ????????? BarleyInitializer?????? ?????????.
        try {
            if(dataPublisher != null) {
                dataPublisher.shutdown();
            }
        } catch (DataEndpointException e) {
            log.error("Error while shut downing Throttle data publisher " + e.getMessage());
        }
        */
        if(executor != null) {
        	if(!executor.isShutdown()) {
        		executor.shutdownNow();
        	}
        }
    }
}

