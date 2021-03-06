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

package barley.apimgt.gateway.internal;

import org.apache.axis2.context.ConfigurationContext;
//import org.wso2.carbon.utils.ConfigurationContextService;

import barley.apimgt.gateway.service.APIThrottleDataService;
import barley.apimgt.gateway.throttling.ThrottleDataHolder;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.APIManagerConfigurationService;
import barley.apimgt.impl.dto.ThrottleProperties;


public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    //private ConfigurationContextService cfgCtxService;
    private APIManagerConfigurationService amConfigService;
    public ThrottleDataHolder throttleDataHolder;
    private ThrottleProperties throttleProperties;
    // (추가) 20220.02.03 - jms 컴포넌트에 전달하기 위해 사용 
    private APIThrottleDataService apiThrottleDataService;
    
    public APIThrottleDataService getApiThrottleDataService() {
		return apiThrottleDataService;
	}

	public void setApiThrottleDataService(APIThrottleDataService apiThrottleDataService) {
		this.apiThrottleDataService = apiThrottleDataService;
	}

	public ThrottleDataHolder getThrottleDataHolder() {
        return throttleDataHolder;
    }

    public void setThrottleDataHolder(ThrottleDataHolder throttleDataHolder) {
        this.throttleDataHolder = throttleDataHolder;
    }

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    /*public void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        this.cfgCtxService = cfgCtxService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return cfgCtxService;
    }

    public ConfigurationContext getServerConfigurationContext() {
        return cfgCtxService.getServerConfigContext();
    }*/
	
    public APIManagerConfiguration getAPIManagerConfiguration() {
        return amConfigService.getAPIManagerConfiguration();
    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {
        return amConfigService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
    }

    public ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }

    public void setThrottleProperties(ThrottleProperties throttleProperties) {
        this.throttleProperties = throttleProperties;
    }
}
