/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package barley.apimgt.keymgt.service.thrift;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.wso2.carbon.core.AbstractAdmin;

import barley.apimgt.api.dto.ConditionDTO;
import barley.apimgt.api.dto.ConditionGroupDTO;
import barley.apimgt.impl.generated.thrift.APIKeyValidationInfoDTO;
import barley.apimgt.impl.generated.thrift.APIKeyValidationService;
import barley.apimgt.impl.generated.thrift.APIManagementException;
import barley.apimgt.impl.generated.thrift.URITemplate;
import barley.apimgt.keymgt.APIKeyMgtException;
import barley.core.MultitenantConstants;
import barley.core.ServerConstants;
import barley.core.context.PrivilegedBarleyContext;
import barley.core.utils.ThriftSession;
import barley.identity.authenticator.thrift.ThriftAuthenticatorService;

public class APIKeyValidationServiceImpl extends AbstractAdmin
        implements APIKeyValidationService.Iface {
    private static Log log = LogFactory.getLog(APIKeyValidationServiceImpl.class);
    /*Handler to ThriftAuthenticatorService which handles authentication to admin services.*/
    private static ThriftAuthenticatorService thriftAuthenticatorService;
    /*Handler to actual entitlement service which is going to be wrapped by thrift interface*/
    private static barley.apimgt.keymgt.service.APIKeyValidationService apiKeyValidationService;

    /**
     * Init the AuthenticationService handler to be used for authentication.
     */
    public static void init(ThriftAuthenticatorService authenticatorService) {
        thriftAuthenticatorService = authenticatorService;
        apiKeyValidationService = new barley.apimgt.keymgt.service.APIKeyValidationService();

    }

    /**
     * CarbonContextHolderBase is thread local. So we need to populate it with the one created
     * at user authentication.
     *
     * @param authSession
     * throws APIKeyMgtException
     */
    private void populateCurrentCarbonContextFromAuthSession(
            PrivilegedBarleyContext carbonContextHolder, ThriftSession authSession) throws barley.apimgt.keymgt.APIKeyMgtException {

        //read parameters from it and set it in current carbon context for this thread
        
        try {
			carbonContextHolder.setUsername((String)(authSession.getAttribute(ServerConstants.AUTHENTICATION_SERVICE_USERNAME)));
			carbonContextHolder.setTenantDomain((String)(authSession.getAttribute(MultitenantConstants.TENANT_DOMAIN)));
	        carbonContextHolder.setTenantId((Integer)(authSession.getAttribute(MultitenantConstants.TENANT_ID)));
		} catch (Exception e) {
			String authErrorMsg = "Error populating current carbon context from thrift auth session: " + e.getMessage();
            log.warn(authErrorMsg);
            throw new APIKeyMgtException(authErrorMsg);
		}
        
        /*carbonContextHolder.setUsername(storedCarbonCtxHolder.getUsername());
        carbonContextHolder.setTenantDomain(storedCarbonCtxHolder.getTenantDomain());
        carbonContextHolder.setTenantId(storedCarbonCtxHolder.getTenantId());
        carbonContextHolder.setRegistry(RegistryType.LOCAL_REPOSITORY,
                storedCarbonCtxHolder.getRegistry(RegistryType.LOCAL_REPOSITORY));
        carbonContextHolder.setRegistry(RegistryType.SYSTEM_CONFIGURATION,
                storedCarbonCtxHolder.getRegistry(RegistryType.SYSTEM_CONFIGURATION));
        carbonContextHolder.setRegistry(RegistryType.SYSTEM_GOVERNANCE,
                storedCarbonCtxHolder.getRegistry(RegistryType.SYSTEM_GOVERNANCE));
        carbonContextHolder.setRegistry(RegistryType.USER_CONFIGURATION,
                storedCarbonCtxHolder.getRegistry(RegistryType.USER_CONFIGURATION));
        carbonContextHolder.setRegistry(RegistryType.USER_GOVERNANCE,
                storedCarbonCtxHolder.getRegistry(RegistryType.USER_GOVERNANCE));
        carbonContextHolder.setUserRealm(storedCarbonCtxHolder.getUserRealm());*/
    }

    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken,
                                               String sessionId,String requiredAuthenticationLevel,
                                               String allowedDomains, String matchingResource, String httpVerb)
            throws barley.apimgt.impl.generated.thrift.APIKeyMgtException, APIManagementException, TException {
        APIKeyValidationInfoDTO thriftKeyValidationInfoDTO = null;
        try {
            if (thriftAuthenticatorService != null && apiKeyValidationService != null) {

                if (thriftAuthenticatorService.isAuthenticated(sessionId)) {

                    //obtain the thrift session for this session id
                    ThriftSession currentSession = thriftAuthenticatorService.getSessionInfo(sessionId);

                    //obtain a dummy carbon context holder
                    PrivilegedBarleyContext carbonContextHolder = PrivilegedBarleyContext.getThreadLocalCarbonContext();
                    int tenantId = PrivilegedBarleyContext.getThreadLocalCarbonContext().getTenantId();
                    String tenantDomain = PrivilegedBarleyContext.getThreadLocalCarbonContext().getTenantDomain();
                    
                    //start tenant flow to stack up any existing carbon context holder base,
                    //and initialize a raw one
                    PrivilegedBarleyContext.startTenantFlow();
                    if (tenantDomain == null) {
                    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,true);
                    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    } else {
                    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                    }

                    try {

                        // need to populate current carbon context from the one created at
                        // authentication
                        populateCurrentCarbonContextFromAuthSession(carbonContextHolder,
                                                                    currentSession);

                        barley.apimgt.impl.dto.APIKeyValidationInfoDTO keyValidationInfoDTO =
                                apiKeyValidationService.validateKey(context, version, accessToken,
                                                                    requiredAuthenticationLevel,allowedDomains,
                                                                    matchingResource, httpVerb);

                        thriftKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
                        thriftKeyValidationInfoDTO.setAuthorized(keyValidationInfoDTO.isAuthorized());
                        thriftKeyValidationInfoDTO.setSubscriber(keyValidationInfoDTO.getSubscriber());
                        thriftKeyValidationInfoDTO.setSubscriptionTier(keyValidationInfoDTO.getSubscriptionTier());
                        thriftKeyValidationInfoDTO.setType(keyValidationInfoDTO.getType());
                        thriftKeyValidationInfoDTO.setEndUserToken(keyValidationInfoDTO.getEndUserToken());
                        thriftKeyValidationInfoDTO.setEndUserName(keyValidationInfoDTO.getEndUserName());
                        thriftKeyValidationInfoDTO.setApplicationName(keyValidationInfoDTO.getApplicationName());
                        thriftKeyValidationInfoDTO.setValidationStatus(keyValidationInfoDTO.getValidationStatus());
                        thriftKeyValidationInfoDTO.setApplicationId(keyValidationInfoDTO.getApplicationId());
                        thriftKeyValidationInfoDTO.setApplicationTier(keyValidationInfoDTO.getApplicationTier());
                        thriftKeyValidationInfoDTO.setApiPublisher(keyValidationInfoDTO.getApiPublisher());
                        thriftKeyValidationInfoDTO.setConsumerKey(keyValidationInfoDTO.getConsumerKey());
                        thriftKeyValidationInfoDTO.setApiName(keyValidationInfoDTO.getApiName());
                        thriftKeyValidationInfoDTO.setIssuedTime(keyValidationInfoDTO.getIssuedTime());
                        thriftKeyValidationInfoDTO.setValidityPeriod(keyValidationInfoDTO.getValidityPeriod());
                        thriftKeyValidationInfoDTO.setAuthorizedDomains(keyValidationInfoDTO.getAuthorizedDomains());
                        thriftKeyValidationInfoDTO.setIsContentAware(keyValidationInfoDTO.isContentAware());
                        thriftKeyValidationInfoDTO.setApiTier(keyValidationInfoDTO.getApiTier());
                        thriftKeyValidationInfoDTO.setThrottlingDataList(keyValidationInfoDTO.getThrottlingDataList());
                        thriftKeyValidationInfoDTO.setSubscriberTenantDomain(keyValidationInfoDTO.getSubscriberTenantDomain());
                        thriftKeyValidationInfoDTO.setSpikeArrestLimit(keyValidationInfoDTO.getSpikeArrestLimit());
                        thriftKeyValidationInfoDTO.setSpikeArrestLimit(keyValidationInfoDTO.getSpikeArrestLimit());
                        thriftKeyValidationInfoDTO.setSpikeArrestUnit(keyValidationInfoDTO.getSpikeArrestUnit());
                        thriftKeyValidationInfoDTO.setStopOnQuotaReach(keyValidationInfoDTO.isStopOnQuotaReach());
                    } finally {
                         PrivilegedBarleyContext.endTenantFlow();
                    }

                } else {
                    String authErrorMsg = "Invalid session id for thrift authenticator.";
                    log.warn(authErrorMsg);
                    throw new barley.apimgt.impl.generated.thrift.APIKeyMgtException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator or APIKeyValidationService is not initialized.";
                log.error(initErrorMsg);
                throw new barley.apimgt.impl.generated.thrift.APIKeyMgtException(initErrorMsg);
            }

        } catch (APIKeyMgtException e) {
            log.error("Error in invoking validate key via thrift..");
            throw new barley.apimgt.impl.generated.thrift.APIKeyMgtException(e.getMessage());
        } catch (barley.apimgt.api.APIManagementException e) {
            log.error("Error in invoking validate key via thrift..");
            throw new APIManagementException(e.getMessage());
        }
        return thriftKeyValidationInfoDTO;
    }
    
    @Override
    public List<URITemplate> getAllURITemplates(String context, String apiVersion, String sessionId)
            throws barley.apimgt.impl.generated.thrift.APIKeyMgtException, APIManagementException, TException {
        ArrayList<URITemplate> templates=new ArrayList<URITemplate>();
        try {
            if (thriftAuthenticatorService != null && apiKeyValidationService != null) {

                if (thriftAuthenticatorService.isAuthenticated(sessionId)) {

                    //obtain the thrift session for this session id
                    ThriftSession currentSession = thriftAuthenticatorService.getSessionInfo(sessionId);

                    //obtain a dummy carbon context holder
                    PrivilegedBarleyContext carbonContextHolder = PrivilegedBarleyContext.getThreadLocalCarbonContext();


                    /*start tenant flow to stack up any existing carbon context holder base,
                    and initialize a raw one*/
                    PrivilegedBarleyContext.startTenantFlow();

                    try {

                        // need to populate current carbon context from the one created at
                        // authentication
                        populateCurrentCarbonContextFromAuthSession(carbonContextHolder,
                                                                    currentSession);

                        ArrayList<barley.apimgt.api.model.URITemplate> uriTemplates =
                                apiKeyValidationService.getAllURITemplates(context, apiVersion);

                        for (barley.apimgt.api.model.URITemplate aDto : uriTemplates) {
                            URITemplate temp = toTemplates(aDto);
                            templates.add(temp);
                        }


                    } finally {
                        PrivilegedBarleyContext.endTenantFlow();
                    }

                } else {
                    String authErrorMsg = "Invalid session id for thrift authenticator.";
                    log.warn(authErrorMsg);
                    throw new APIKeyMgtException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator or APIKeyValidationService is not initialized.";
                log.error(initErrorMsg);
                throw new APIKeyMgtException(initErrorMsg);
            }

        } catch (APIKeyMgtException e) {
            log.error("Error in invoking validate key via thrift..");
            throw new barley.apimgt.impl.generated.thrift.APIKeyMgtException(e.getMessage());
        } catch (barley.apimgt.api.APIManagementException e) {
            log.error("Error in invoking validate key via thrift..");
            throw new APIManagementException(e.getMessage());
        }
        return templates;
    }

    private URITemplate toTemplates(
    		barley.apimgt.api.model.URITemplate dto) {
        URITemplate template = new URITemplate();
        template.setAuthType(dto.getAuthType());
        template.setHttpVerb(dto.getHTTPVerb());
        template.setResourceSandboxURI(dto.getResourceSandboxURI());
        template.setUriTemplate(dto.getUriTemplate());
        template.setThrottlingTier(dto.getThrottlingTier());
        template.setThrottlingConditions(dto.getThrottlingConditions());

        // Converting ConditionGroupDTO s to the type compatible with thrift.
        ConditionGroupDTO[] conditionGroups = dto.getConditionGroups();
        if (conditionGroups != null) {
            ArrayList<barley.apimgt.impl.generated.thrift.ConditionGroupDTO> conditionGroupsThrift = new
                    ArrayList<barley.apimgt.impl.generated.thrift.ConditionGroupDTO>(conditionGroups.length);

            for (ConditionGroupDTO conditionGroup : conditionGroups) {
            	barley.apimgt.impl.generated.thrift.ConditionGroupDTO conditionGroupThrift = new barley.apimgt.impl.
            			generated.thrift.ConditionGroupDTO();
                conditionGroupThrift.setConditionGroupId(conditionGroup.getConditionGroupId());
                ConditionDTO[] conditions = conditionGroup.getConditions();
                if (conditions != null) {
                    ArrayList<barley.apimgt.impl.generated.thrift.ConditionDTO> conditionsThrift = new
                            ArrayList<barley.apimgt.impl.generated.thrift.ConditionDTO>(conditions.length);
                    for (ConditionDTO condition : conditions) {
                    	barley.apimgt.impl.generated.thrift.ConditionDTO conditionThrift = new barley.apimgt.impl.
                    			generated.thrift.ConditionDTO();
                        conditionThrift.setConditionType(condition.getConditionType());
                        conditionThrift.setConditionName(condition.getConditionName());
                        conditionThrift.setConditionValue(condition.getConditionValue());
                        conditionThrift.setIsInverted(condition.isInverted());
                        conditionsThrift.add(conditionThrift);
                    }
                    conditionGroupThrift.setConditions(conditionsThrift);
                }
                conditionGroupsThrift.add(conditionGroupThrift);
            }
            template.setConditionGroups(conditionGroupsThrift);
        }
        template.setApplicableLevel(dto.getApplicableLevel());
        return template;
    }


}
