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
 * This class is used as a bean for represent API response time statistics result from the DAS REST API
 */
public class ResponseTimesByAPIsValue {
    private int totalServiceTime;
    private int totalResponseCount;

    private List<String> api_version_context_facet;

    public int getTotalServiceTime() {
        return totalServiceTime;
    }

    public void setCount_sum(int totalServiceTime) {
        this.totalServiceTime = totalServiceTime;
    }

    public int getTotalResponseCount() {
        return totalResponseCount;
    }

    public void setTotalResponseCount(int totalResponseCount) {
        this.totalResponseCount = totalResponseCount;
    }

    public List<String> getColumnNames() {
        return api_version_context_facet;
    }

    public void setColumnNames(List<String> ColumnNames) {
        this.api_version_context_facet = ColumnNames;
    }

    public ResponseTimesByAPIsValue(int totalServiceTime, int totalResponseCount, List<String> ColumnNames) {
        super();
        this.totalServiceTime = totalServiceTime;
        this.totalResponseCount = totalResponseCount;
        this.api_version_context_facet = ColumnNames;
    }
}
