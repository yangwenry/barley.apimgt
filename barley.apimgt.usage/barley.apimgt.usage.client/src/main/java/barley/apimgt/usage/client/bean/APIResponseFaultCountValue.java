/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package barley.apimgt.usage.client.bean;

import java.util.List;

/**
 * This class is used as a bean for represent API fault count result from the DAS REST API
 */
public class APIResponseFaultCountValue {
    private int totalFaultCount;
    private List<String> api_version_apiPublisher_context_facet;

    public APIResponseFaultCountValue(int totalFaultCount, List<String> api_version_userId_facet) {
        this.totalFaultCount = totalFaultCount;
        this.api_version_apiPublisher_context_facet = api_version_userId_facet;
    }

    public int getTotalFaultCount() {
        return totalFaultCount;
    }

    public void setTotalFaultCount(int totalFaultCount) {
        this.totalFaultCount = totalFaultCount;
    }

    public List<String> getColumnNames() {
        return api_version_apiPublisher_context_facet;
    }

    public void setColumnNames(List<String> api_version_userId_facet) {
        this.api_version_apiPublisher_context_facet = api_version_userId_facet;
    }

}
