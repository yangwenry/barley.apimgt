/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package barley.apimgt.usage.client;

/**
 * This class is used as a DTO to represent developers over time
 */
public class SubscriptionOverTimeDTO {
    private long time;
    private long count;
    private String apiName;
    private String version;

    public SubscriptionOverTimeDTO(long time, long count, String apiName, String version) {
        this.time = time;
        this.count = count;
        this.apiName = apiName;
        this.version = version;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }



	@Override
	public String toString() {
		return "SubscriptionOverTimeDTO [time=" + time + ", count=" + count + ", apiName=" + apiName + ", version=" + version + "]";
	}  
    
}
