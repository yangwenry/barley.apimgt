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

package barley.apimgt.usage.client.dto;

public class APIResponseFaultCountDTO {

    private String apiName;

    private String version;

    private String context;

    private double faultPercentage;

    private String requestTime;

    private long count;

    private long totalRequestCount;

    private String appName;

    private String consumerKey;

    public String getconsumerKey() {
        return consumerKey;
    }

    public void setconsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getappName() {
        return appName;
    }

    public void setappName(String appName) {
        this.appName = appName;
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getFaultPercentage() {
        return faultPercentage;
    }

    public void setFaultPercentage(double faultPercentage) {
        this.faultPercentage = faultPercentage;
    }

    public long getTotalRequestCount() {
        return totalRequestCount;
    }

    public void setTotalRequestCount(long totalRequestCount) {
        this.totalRequestCount = totalRequestCount;
    }

	@Override
	public String toString() {
		return "APIResponseFaultCountDTO [apiName=" + apiName + ", version=" + version + ", context=" + context
				+ ", faultPercentage=" + faultPercentage + ", requestTime=" + requestTime + ", count=" + count
				+ ", totalRequestCount=" + totalRequestCount + ", appName=" + appName + ", consumerKey=" + consumerKey
				+ "]";
	}
    
    
}
