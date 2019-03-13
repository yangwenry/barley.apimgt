/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package barley.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;

@Deprecated
public class RESTAPIAdminClient {
	
	//private RestApiAdminStub restApiAdminStub;
    private String qualifiedName;
    private String qualifiedDefaultApiName;

    
    static final String backendURLl = "local:///services/";
    
    public RESTAPIAdminClient(String apiProviderName, String apiName, String version) throws AxisFault {
        this.qualifiedName = apiProviderName + "--" + apiName + ":v" + version;
        this.qualifiedDefaultApiName = apiProviderName + "--" + apiName;        
        //restApiAdminStub = new RestApiAdminStub(null, backendURLl + "RestApiAdmin");
    }

    
    public void addDefaultAPI(String apiConfig) throws AxisFault {

        try {
        	// (임시주석) 웹서비스를 구현 중이기에 lib형태로 바로 호출한다.
        	//restApiAdminStub.addApiFromString(apiConfig);
        	//RestApiAdminService service = new RestApiAdminService();
        	//service.addApiFromString(apiConfig);
        	
        } catch (Exception e) {
            throw new AxisFault("Error publishing default API to the Gateway. " + e.getMessage(), e);
        }
    }

	

}
