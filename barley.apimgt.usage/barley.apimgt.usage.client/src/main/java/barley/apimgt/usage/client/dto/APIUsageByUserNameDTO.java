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

public class APIUsageByUserNameDTO {

    private String userId;

    private int requestCount;

    private int throttleOutCount;

    private int faultCount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getThrottleOutCount() {
        return throttleOutCount;
    }

    public void setThrottleOutCount(int throttleOutCount) {
        this.throttleOutCount = throttleOutCount;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    @Override
	public String toString() {
		return "APIUsageByUserNameDTO [userId=" + userId + ", requestCount=" + requestCount + ", throttleOutCount=" + throttleOutCount + ", faultCount="
				+ faultCount + "]";
	}
    
    
}
