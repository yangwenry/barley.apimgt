/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package barley.apimgt.impl.executors;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.APIProvider;
import barley.apimgt.api.FaultGatewaysException;
import barley.apimgt.api.model.API;
import barley.apimgt.api.model.APIStatus;
import barley.apimgt.api.model.Tier;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerFactory;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.impl.utils.APIVersionComparator;
import barley.core.MultitenantConstants;
import barley.core.context.BarleyContext;
import barley.core.context.PrivilegedBarleyContext;
import barley.core.multitenancy.MultitenantUtils;
import barley.governance.api.generic.GenericArtifactManager;
import barley.governance.api.generic.dataobjects.GenericArtifact;
import barley.governance.registry.extensions.aspects.utils.LifecycleConstants;
import barley.governance.registry.extensions.interfaces.Execution;
import barley.registry.core.Registry;
import barley.registry.core.Resource;
import barley.registry.core.exceptions.RegistryException;
import barley.registry.core.jdbc.handlers.RequestContext;
import barley.user.api.UserStoreException;

/**
 * This class is an implementation of the
 * interface {@link org.wso2.carbon.governance.registry.extensions.interfaces.Execution}
 * This class consists methods that will create, prototype, publish, block, deprecate and
 * retire  an API to API Manager.
 * <p/>
 * This executor used to publish a service to API store as a API.
 *
 * @see org.wso2.carbon.governance.registry.extensions.interfaces.Execution
 */
public class APIExecutor implements Execution {

    Log log = LogFactory.getLog(APIExecutor.class);

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user.
     *                     These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     */
    public void init(Map parameterMap) {
    }

    /**
     * @param context      The request context that was generated from the registry core.
     *                     The request context contains the resource, resource path and other
     *                     variables generated during the initial call.
     * @param currentState The current lifecycle state.
     * @param targetState  The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     */
    public boolean execute(RequestContext context, String currentState, String targetState) {
        boolean executed = false;
        String user = PrivilegedBarleyContext.getThreadLocalCarbonContext().getUsername();
        String domain = BarleyContext.getThreadLocalCarbonContext().getTenantDomain();
     
        String userWithDomain = user;
        if(!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)){
            userWithDomain = user + APIConstants.EMAIL_DOMAIN_SEPARATOR + domain;
        }       
        
        userWithDomain = APIUtil.replaceEmailDomainBack(userWithDomain);

        try {
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(userWithDomain);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(domain);

            GenericArtifactManager artifactManager = APIUtil
                    .getArtifactManager(context.getSystemRegistry(), APIConstants.API_KEY);
            Registry registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);
            Resource apiResource = context.getResource();
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                return executed;
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            API api = APIUtil.getAPI(apiArtifact);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userWithDomain);

            APIStatus oldStatus = APIUtil.getApiStatus(apiArtifact.getLifecycleState());
            APIStatus newStatus = APIUtil.getApiStatus(targetState);

            if(newStatus != null){ //only allow the executor to be used with default LC states transition
                                   //check only the newStatus so this executor can be used for LC state change from 
                                   //custom state to default api state
                if ((APIStatus.CREATED.equals(oldStatus) || APIStatus.PROTOTYPED.equals(oldStatus))
                        && APIStatus.PUBLISHED.equals(newStatus)) {
                    Set<Tier> tiers = api.getAvailableTiers();
                    String endPoint = api.getEndpointConfig();
                    if (endPoint != null && endPoint.trim().length() > 0) {
                        if (tiers == null || tiers.size() <= 0) {
                                throw new APIManagementException("Failed to publish service to API store while executing " +
                                                                 "APIExecutor. No Tiers selected");
                        }
                    } else {
                        throw new APIManagementException("Failed to publish service to API store while executing"
                                + " APIExecutor. No endpoint selected");
                    }
                }
            
                //push the state change to gateway
                Map<String, String> failedGateways = apiProvider.propergateAPIStatusChangeToGateways(api.getId(), newStatus);
                
                if (log.isDebugEnabled()) {
                    String logMessage = "Publish changed status to the Gateway. API Name: " + api.getId().getApiName()
                            + ", API Version " + api.getId().getVersion() + ", API Context: " + api.getContext()
                            + ", New Status : " + newStatus;
                    log.debug(logMessage);
                }

                //update api related information for state change
                executed = apiProvider.updateAPIforStateChange(api.getId(), newStatus, failedGateways);

                // Setting resource again to the context as it's updated within updateAPIStatus method
                String apiPath = APIUtil.getAPIPath(api.getId());

                apiResource = registry.get(apiPath);
                context.setResource(apiResource);

                if (log.isDebugEnabled()) {
                    String logMessage =
                            "API related information successfully updated. API Name: " + api.getId().getApiName()
                                    + ", API Version " + api.getId().getVersion() + ", API Context: " + api.getContext()
                                    + ", New Status : " + newStatus;
                    log.debug(logMessage);
                }
            } else {
                throw new APIManagementException("Invalid Lifecycle status for default APIExecutor :" + targetState);
            }
           
            
            boolean deprecateOldVersions = false;
            boolean makeKeysForwardCompatible = false;
            //If the API status is CREATED/PROTOTYPED ,check for check list items of lifecycle
            if (APIStatus.CREATED.equals(oldStatus)|| APIStatus.PROTOTYPED.equals(oldStatus)) {
                deprecateOldVersions = apiArtifact.isLCItemChecked(0, APIConstants.API_LIFE_CYCLE);
                makeKeysForwardCompatible = !(apiArtifact.isLCItemChecked(1, APIConstants.API_LIFE_CYCLE));
            }
            
            if ((APIStatus.CREATED.equals(oldStatus) || APIStatus.PROTOTYPED.equals(oldStatus))
                    && APIStatus.PUBLISHED.equals(newStatus)) {
                if (makeKeysForwardCompatible) {
                    apiProvider.makeAPIKeysForwardCompatible(api);
                }                
                if(deprecateOldVersions) {
                    String provider = APIUtil.replaceEmailDomain(api.getId().getProviderName());

                    List<API> apiList = apiProvider.getAPIsByProvider(provider);
                    APIVersionComparator versionComparator = new APIVersionComparator();
                    for (API oldAPI : apiList) {
                        if (oldAPI.getId().getApiName().equals(api.getId().getApiName()) &&
                                versionComparator.compare(oldAPI, api) < 0 &&
                                (oldAPI.getStatus().equals(APIStatus.PUBLISHED))) {
                            apiProvider.changeLifeCycleStatus(oldAPI.getId(), APIConstants.API_LC_ACTION_DEPRECATE);
                            
                        }
                    }            
                }            
            }
        } catch (RegistryException e) {
            log.error("Failed to get the generic artifact while executing APIExecutor. ", e);
            context.setProperty(LifecycleConstants.EXECUTOR_MESSAGE_KEY,
                                "APIManagementException:" + e.getMessage());
        } catch (APIManagementException e) {
            log.error("Failed to publish service to API store while executing APIExecutor. ", e);
            context.setProperty(LifecycleConstants.EXECUTOR_MESSAGE_KEY,
                                "APIManagementException:" + e.getMessage());
        } catch (FaultGatewaysException e) {
            log.error("Failed to publish service gateway while executing APIExecutor. ", e);
            context.setProperty(LifecycleConstants.EXECUTOR_MESSAGE_KEY,
                                "FaultGatewaysException:" + e.getFaultMap());
        } catch (UserStoreException e) {
            log.error("Failed to get tenant Id while executing APIExecutor. ", e);
            context.setProperty(LifecycleConstants.EXECUTOR_MESSAGE_KEY,
                                "APIManagementException:" + e.getMessage());
        }
        return executed;
    }

    
   
}
