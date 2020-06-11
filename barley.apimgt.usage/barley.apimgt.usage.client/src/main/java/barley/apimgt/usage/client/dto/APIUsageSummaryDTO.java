/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package barley.apimgt.usage.client.dto;

public class APIUsageSummaryDTO {

    private String apiName;
    private String apiPublisher;
    private int requestCount;
    private int successRequestCount;
    private int throttleOutCount;
    private int faultCount;
    private long backendLatency;
    private String time;
    private String version;

    public APIUsageSummaryDTO(String apiName, String version, String apiPublisher, int requestCount, int successRequestCount, int throttleOutCount,
                                            int faultCount, long backendLatency, String time) {
        this.apiName = apiName;
        this.version = version;
        this.apiPublisher = apiPublisher;
        this.requestCount = requestCount;
        this.successRequestCount = successRequestCount;
        this.throttleOutCount = throttleOutCount;
        this.faultCount = faultCount;
        this.backendLatency = backendLatency;
        this.time = time;
    }

    public APIUsageSummaryDTO(String apiName, String version, String apiPublisher, int requestCount, int successRequestCount, int throttleOutCount,
                                            int faultCount, long backendLatency) {
        this.apiName = apiName;
        this.version = version;
        this.apiPublisher = apiPublisher;
        this.requestCount = requestCount;
        this.successRequestCount = successRequestCount;
        this.throttleOutCount = throttleOutCount;
        this.faultCount = faultCount;
        this.backendLatency = backendLatency;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiPublisher() {
        return apiPublisher;
    }

    public void setApiPublisher(String apiPublisher) {
        this.apiPublisher = apiPublisher;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public void setSuccessRequestCount(int successRequestCount) {
        this.successRequestCount = successRequestCount;
    }

    public void setThrottleOutCount(int throttleOutCount) {
        this.throttleOutCount = throttleOutCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getThrottleOutCount() {
        return throttleOutCount;
    }

    public int getSuccessRequestCount() {
        return successRequestCount;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public String getTime() {
        return time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getBackendLatency() {
        return backendLatency;
    }

    public void setBackendLatency(long backendLatency) {
        this.backendLatency = backendLatency;
    }

    @Override
	public String toString() {
		return "APIUsageSummaryDTO [apiName=" + apiName + ", apiPublisher=" + apiPublisher + ", version=" + version
                + ", requestCount=" + requestCount + ", successRequestCount=" + successRequestCount
                + ", throttleOutCount=" + throttleOutCount + ", faultCount=" + faultCount + ", backendLatency=" + backendLatency
                + ", time=" + time + "]";
	}
    
    
    
}
