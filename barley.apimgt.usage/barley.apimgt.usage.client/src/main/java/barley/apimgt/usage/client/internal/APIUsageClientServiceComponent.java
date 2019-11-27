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

package barley.apimgt.usage.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.osgi.service.component.ComponentContext;

import barley.apimgt.impl.APIManagerAnalyticsConfiguration;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.APIManagerConfigurationService;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.usage.client.UsageClient;
import barley.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;

/**
 * @scr.component name="org.wso2.apimgt.usage.client" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class APIUsageClientServiceComponent {

    private static final Log log = LogFactory.getLog(APIUsageClientServiceComponent.class);

    private static APIManagerConfiguration configuration = null;

    private static APIManagerAnalyticsConfiguration analyticsConfiguration = null;

    public void activate()
            throws APIMgtUsageQueryServiceClientException {
        if (log.isDebugEnabled()) {
            log.debug("API usage client component activated");
        }
        if (APIUtil.isAnalyticsEnabled()){
            UsageClient.initializeDataSource();
        }
    }

    public void deactivate() {
        log.debug("API usage client component deactivated");
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        log.debug("API manager configuration service bound to the API usage client component");
        configuration = amcService.getAPIManagerConfiguration();
        analyticsConfiguration = amcService.getAPIAnalyticsConfiguration();
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        log.debug("API manager configuration service unbound from the API usage client component");
    }

    public static APIManagerConfiguration getAPIManagerConfiguration() {
        return configuration;
    }

    public static APIManagerAnalyticsConfiguration getAnalyticsConfiguration() {
        return analyticsConfiguration;
    }
}
