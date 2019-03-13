/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package barley.apimgt.impl.workflow;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.WorkflowResponse;
import barley.apimgt.api.model.AccessTokenInfo;
import barley.apimgt.api.model.AccessTokenRequest;
import barley.apimgt.api.model.Application;
import barley.apimgt.api.model.KeyManager;
import barley.apimgt.api.model.OAuthApplicationInfo;
import barley.apimgt.api.model.Subscriber;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.dao.ApiMgtDAO;
import barley.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import barley.apimgt.impl.dto.WorkflowDTO;
import barley.apimgt.impl.factory.KeyManagerHolder;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.impl.utils.ApplicationUtils;

public abstract class AbstractApplicationRegistrationWorkflowExecutor extends WorkflowExecutor{

    private static final Log log = LogFactory.getLog(AbstractApplicationRegistrationWorkflowExecutor.class);
    
    public String getWorkflowType(){
       return WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION;
    }

    public WorkflowResponse execute(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Executing AbstractApplicationRegistrationWorkflowExecutor...");
        }
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            //dao.createApplicationRegistrationEntry((ApplicationRegistrationWorkflowDTO) workFlowDTO, false);
            ApplicationRegistrationWorkflowDTO appRegDTO;
            if (workFlowDTO instanceof ApplicationRegistrationWorkflowDTO) {
                appRegDTO = (ApplicationRegistrationWorkflowDTO)workFlowDTO;
            }else{
                String message = "Invalid workflow type found";
                log.error(message);
                throw new WorkflowException(message);
            }
            dao.createApplicationRegistrationEntry(appRegDTO,false);
           // appRegDTO.getAppInfoDTO().saveDTO();
            super.execute(workFlowDTO);
        } catch (APIManagementException e) {
            log.error("Error while creating Application Registration entry.", e);
            throw new WorkflowException("Error while creating Application Registration entry.", e);
        }
        return new GeneralWorkflowResponse();
    }

    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Completing AbstractApplicationRegistrationWorkflowExecutor...");
        }
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            String status = null;
            if ("CREATED".equals(workFlowDTO.getStatus().toString())) {
                status = APIConstants.AppRegistrationStatus.REGISTRATION_CREATED;
            } else if ("REJECTED".equals(workFlowDTO.getStatus().toString())) {
                status = APIConstants.AppRegistrationStatus.REGISTRATION_REJECTED;
            } else if ("APPROVED".equals(workFlowDTO.getStatus().toString())) {
                status = APIConstants.AppRegistrationStatus.REGISTRATION_APPROVED;
            }

            ApplicationRegistrationWorkflowDTO regWorkFlowDTO;
            if (workFlowDTO instanceof ApplicationRegistrationWorkflowDTO) {
                regWorkFlowDTO = (ApplicationRegistrationWorkflowDTO)workFlowDTO;
            } else {
                String message = "Invalid workflow type found";
                log.error(message);
                throw new WorkflowException(message);
            }
            dao.populateAppRegistrationWorkflowDTO(regWorkFlowDTO);

            dao.updateApplicationRegistration(status, regWorkFlowDTO.getKeyType(),
                    regWorkFlowDTO.getApplication().getId());

        } catch (APIManagementException e) {
            log.error("Error while completing Application Registration entry.", e);
            throw new WorkflowException("Error while completing Application Registration entry.", e);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * This method will create a oAuth client at oAuthServer.
     * and will create a mapping with APIM using consumerKey
     * @param workflowDTO
     * @throws APIManagementException
     */
    protected void generateKeysForApplication(ApplicationRegistrationWorkflowDTO workflowDTO) throws
                                                                                              APIManagementException {
        //ApiMgtDAO dao = ApiMgtDAO.getInstance();
        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            dogenerateKeysForApplication(workflowDTO);
        }
    }

    public static void dogenerateKeysForApplication(ApplicationRegistrationWorkflowDTO workflowDTO)
            throws APIManagementException{
        log.debug("Registering Application and creating an Access Token... ");
        Application application = workflowDTO.getApplication();
        Subscriber subscriber = application.getSubscriber();
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        if (subscriber == null || workflowDTO.getAllowedDomains() == null) {
            dao.populateAppRegistrationWorkflowDTO(workflowDTO);
        }

        try {
            //get new key manager
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();

            workflowDTO.getAppInfoDTO().getOAuthApplicationInfo()
                       .setClientName(application.getName());
            
            // oauth 어플을 생성하고 consumerkey와 consumerSecret를 가져온다. 
            //createApplication on oAuthorization server.
            OAuthApplicationInfo oAuthApplication = keyManager.createApplication(workflowDTO.getAppInfoDTO());

            //update associateApplication
            ApplicationUtils.updateOAuthAppAssociation(application, workflowDTO.getKeyType(), oAuthApplication);

            //change create application status in to completed.
            dao.updateApplicationRegistration(APIConstants.AppRegistrationStatus.REGISTRATION_COMPLETED,
                    workflowDTO.getKeyType(), workflowDTO.getApplication().getId());

            workflowDTO.setApplicationInfo(oAuthApplication);

            AccessTokenRequest tokenRequest = ApplicationUtils.createAccessTokenRequest(oAuthApplication, null);
            // 토큰을 생성하고 가져온다. 
            AccessTokenInfo tokenInfo = keyManager.getNewApplicationAccessToken(tokenRequest);

            workflowDTO.setAccessTokenInfo(tokenInfo);
        } catch (Exception e) {
            APIUtil.handleException("Error occurred while executing SubscriberKeyMgtClient.", e);
        }
    }

}
