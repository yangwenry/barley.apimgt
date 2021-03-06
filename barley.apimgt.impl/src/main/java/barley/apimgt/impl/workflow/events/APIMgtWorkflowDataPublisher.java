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

package barley.apimgt.impl.workflow.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerAnalyticsConfiguration;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.dto.WorkflowDTO;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.APIUtil;
import barley.core.context.BarleyContext;
import barley.databridge.agent.DataPublisher;
import barley.databridge.agent.exception.DataEndpointAgentConfigurationException;
import barley.databridge.agent.exception.DataEndpointAuthenticationException;
import barley.databridge.agent.exception.DataEndpointConfigurationException;
import barley.databridge.agent.exception.DataEndpointException;
import barley.databridge.commons.exception.TransportException;

/*
* This class will act as data-publisher for workflow events.Reason for not re-using the usage
* publisher bundle is there's a maven cyclic dependency appears if we import it from impl bundle.
*/

public class APIMgtWorkflowDataPublisher {

    private static final Log log = LogFactory.getLog(APIMgtWorkflowDataPublisher.class);
    private DataPublisher dataPublisher;
    private static Map<String, DataPublisher> dataPublisherMap;
    static APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIManagerConfiguration();
    static APIManagerAnalyticsConfiguration analyticsConfig = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIAnalyticsConfiguration();
    boolean enabled = APIUtil.isAnalyticsEnabled();
    private static String wfStreamName;
    private static String wfStreamVersion;

    public APIMgtWorkflowDataPublisher() {
        if (!enabled) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
        }

        wfStreamName = config.getFirstProperty(APIConstants.API_WF_STREAM_NAME);
        wfStreamVersion = config.getFirstProperty(APIConstants.API_WF_STREAM_VERSION);
        if (wfStreamName == null || wfStreamVersion == null) {
            log.error("Workflow stream name or version is null. Check api-manager.xml");
        }

        dataPublisherMap = new ConcurrentHashMap<String, DataPublisher>();
        this.dataPublisher = getDataPublisher();
    }

    private static DataPublisher getDataPublisher() {

        String tenantDomain = BarleyContext.getThreadLocalCarbonContext().getTenantDomain();

        //Get DataPublisher which has been registered for the tenant.
        DataPublisher dataPublisher = getDataPublisher(tenantDomain);
        String bamServerURL = analyticsConfig.getDasReceiverUrlGroups();
        String bamServerAuthURL = analyticsConfig.getDasReceiverAuthUrlGroups();
        String bamServerUser = analyticsConfig.getDasReceiverServerUser();
        String bamServerPassword = analyticsConfig.getDasReceiverServerPassword();

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisher == null) {

            try {
                dataPublisher = new DataPublisher(null, bamServerURL, bamServerAuthURL, bamServerUser,
                        bamServerPassword);

                //Add created DataPublisher.
                addDataPublisher(tenantDomain, dataPublisher);
            } catch (DataPublisherAlreadyExistsException e) {
                log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
                         " when one already exists. Returning existing data publisher");
                return getDataPublisher(tenantDomain);
            } catch (DataEndpointConfigurationException e) {
                log.error("Error while creating data publisher",e);
            } catch (DataEndpointException e) {
                log.error("Error while creating data publisher",e);
            } catch (DataEndpointAgentConfigurationException e) {
                log.error("Error while creating data publisher",e);
            } catch (TransportException e) {
                log.error("Error while creating data publisher",e);
            } catch (DataEndpointAuthenticationException e) {
                log.error("Error while creating data publisher",e);
            }
        }

        return dataPublisher;
    }

    public boolean publishEvent(WorkflowDTO workflowDTO) {
        try {
            if (!enabled) {
                return true;
            }

            if (workflowDTO != null) {
                try {

                    dataPublisher.publish(getStreamID(), System.currentTimeMillis(), new Object[]{"external"},
                            null, (Object[]) createPayload(workflowDTO));
                } catch (Exception e) {
                    log.error("Error while publishing workflow event" +
                              workflowDTO.getWorkflowReference(), e);
                }
            }
        } catch (Exception e) {
            log.error("Cannot publish workflow event. " + e.getMessage(), e);
        }
        return true;
    }

    public Object createPayload(WorkflowDTO workflowDTO) {
        return new Object[]{workflowDTO.getWorkflowReference(),
                            workflowDTO.getStatus().toString(), workflowDTO.getTenantDomain(),
                            workflowDTO.getWorkflowType(), workflowDTO.getCreatedTime(),
                            workflowDTO.getUpdatedTime()};
    }

    public static String getWFStreamName() {
        return wfStreamName;
    }

    public static String getStreamID() {
        return getWFStreamName() + ":"+ getWFStreamVersion();
    }

    public static String getWFStreamVersion() {
        return wfStreamVersion;
    }

    /**
     * Fetch the data publisher which has been registered under the tenant domain.
     *
     * @param tenantDomain - The tenant domain under which the data publisher is registered
     * @return - Instance of the DataPublisher which was registered. Null if not registered.
     */
    public static DataPublisher getDataPublisher(String tenantDomain) {
        if (dataPublisherMap.containsKey(tenantDomain)) {
            return dataPublisherMap.get(tenantDomain);
        }
        return null;
    }

    /**
     * Adds a DataPublisher to the data publisher map.
     *
     * @param tenantDomain  - The tenant domain under which the data publisher will be registered.
     * @param dataPublisher - Instance of the DataPublisher
     * @throws org.wso2.carbon.apimgt.impl.workflow.events.DataPublisherAlreadyExistsException
     *          - If a data publisher has already been registered under the
     *          tenant domain
     */
    public static void addDataPublisher(String tenantDomain,
                                        DataPublisher dataPublisher)
            throws DataPublisherAlreadyExistsException {
        if (dataPublisherMap.containsKey(tenantDomain)) {
            throw new DataPublisherAlreadyExistsException("A DataPublisher has already been created for the tenant " +
                                                          tenantDomain);
        }

        dataPublisherMap.put(tenantDomain, dataPublisher);
    }

}
