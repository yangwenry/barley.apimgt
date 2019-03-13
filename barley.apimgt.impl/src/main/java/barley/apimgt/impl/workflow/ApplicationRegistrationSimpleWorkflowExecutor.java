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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.WorkflowResponse;
import barley.apimgt.impl.dao.ApiMgtDAO;
import barley.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import barley.apimgt.impl.dto.WorkflowDTO;

/**
 * This is a simple work flow extension to have Application registration process
 * 
 */
public class ApplicationRegistrationSimpleWorkflowExecutor extends AbstractApplicationRegistrationWorkflowExecutor {

	private static final Log log =
	                               LogFactory.getLog(ApplicationRegistrationSimpleWorkflowExecutor.class);
	/**
	 * Execute the workflow executor
	 *
	 * @param workFlowDTO
	 *            - {@link org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO}
	 * @throws org.wso2.carbon.apimgt.impl.workflow.WorkflowException
	 */

    public WorkflowResponse execute(WorkflowDTO workFlowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.info("Executing Application creation Workflow..");
        }

        workFlowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workFlowDTO);
        super.publishEvents(workFlowDTO);
		return new GeneralWorkflowResponse();
    }

	/**
	 * Complete the external process status
	 * Based on the workflow status we will update the status column of the
	 * Application table
	 * 
	 * @param workFlowDTO - WorkflowDTO
	 */
	public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
		if (log.isDebugEnabled()) {
			log.info("Complete  Application Registration Workflow..");
		}

        ApplicationRegistrationWorkflowDTO regWFDTO = (ApplicationRegistrationWorkflowDTO) workFlowDTO;
		

		ApiMgtDAO dao = ApiMgtDAO.getInstance();

		try {
            dao.createApplicationRegistrationEntry((ApplicationRegistrationWorkflowDTO)workFlowDTO, false);
            // (수정) 2018.01.06 어플리케이션등록 요청시 키생성은 하지 않도록 수정한다. 
            //generateKeysForApplication(regWFDTO);
		} catch (APIManagementException e) {
			String msg = "Error occurred when updating the status of the Application creation process";
			log.error(msg, e);
			throw new WorkflowException(msg, e);
		}
		return new GeneralWorkflowResponse();
	}

	@Override
	public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
		return null;
	}

}
