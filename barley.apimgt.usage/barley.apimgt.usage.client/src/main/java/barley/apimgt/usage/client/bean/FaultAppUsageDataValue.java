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
 * This class is used as a bean for represent API Fault count usage statistics result from the DAS REST API
 */
public class FaultAppUsageDataValue {
    private long count;
    private List<String> consumerKey_api_facet;

    public FaultAppUsageDataValue(long count, List<String> columnNames) {
        super();
        this.count = count;
        this.consumerKey_api_facet = columnNames;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<String> getColumnNames() {
        return consumerKey_api_facet;
    }

    public void setColumnNames(List<String> columnNames) {
        this.consumerKey_api_facet = columnNames;
    }
}
