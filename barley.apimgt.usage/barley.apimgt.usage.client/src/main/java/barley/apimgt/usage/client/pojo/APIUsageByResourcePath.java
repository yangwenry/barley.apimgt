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
package barley.apimgt.usage.client.pojo;

/**
 * This class is used as a pojo class to represent API usage by resource path
 */
public class APIUsageByResourcePath {
    private String apiName;
    private String apiVersion;
    private String method;
    private String context;
    private long requestCount;
    private String time;
    private String resourcePath;

    public APIUsageByResourcePath(String apiName, String apiVersion, String method, String context, long requestCount,
            String time,String resourcePath) {
        this.apiName = apiName;
        this.apiVersion = apiVersion;
        this.method = method;
        this.context = context;
        this.requestCount = requestCount;
        this.time = time;
        this.resourcePath = resourcePath;
    }

    public APIUsageByResourcePath() {

    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

	@Override
	public String toString() {
		return "APIUsageByResourcePath [apiName=" + apiName + ", apiVersion=" + apiVersion + ", method=" + method
				+ ", context=" + context + ", requestCount=" + requestCount + ", time=" + time + ", resourcePath="
				+ resourcePath + "]";
	}
    
    
}
