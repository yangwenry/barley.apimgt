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

package barley.apimgt.gateway.handlers.security;

import java.util.List;

/**
 * Contains some context information related to an authenticated request. This can be used
 * to access API keys and tier information related to already authenticated requests.
 */
public class AuthenticationContext {

    private boolean authenticated;
    private String username;
    private String applicationTier;
    // (수정) 2019.06.20 - 용어가 불분명하여 subscriptionTier로 변경
    //private String tier;
    private String subscriptionTier;
    private String apiTier;
    private boolean isContentAwareTierPresent;
    private String apiKey;
    private String keyType;
    private String callerToken;
    private String applicationId;
    private String applicationName;
    private String consumerKey;
    private String subscriber;
    private List<String> throttlingDataList;
    private int spikeArrestLimit;
    private String subscriberTenantDomain;
    private String spikeArrestUnit;
    private boolean stopOnQuotaReach;
    // (추가) 2020.05.19 - 데이터분석기에서 쓰로틀링 정책 limit를 전달하기 위해 추가
    private long applicationThrottleLimit;

    public List<String> getThrottlingDataList() {
        return throttlingDataList;
    }

    public void setThrottlingDataList(List<String> throttlingDataList) {
        this.throttlingDataList = throttlingDataList;
    }
    //Following throttle data list can be use to hold throttle data and api level throttle key
    //should be its first element.

    public boolean isContentAwareTierPresent() {
        return isContentAwareTierPresent;
    }

    public void setIsContentAware(boolean isContentAware) {
        this.isContentAwareTierPresent = isContentAware;
    }

    public String getApiTier() {
        return apiTier;
    }

    public void setApiTier(String apiTier) {
        this.apiTier = apiTier;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() {
        return username;
    }

    
    /* (아래코드로 수정)
    public String getTier() {
        return tier;
    }
    
    public void setTier(String tier) {
        this.tier = tier;
    }*/
    
    public String getSubscriptionTier() {
        return subscriptionTier;
    }
    
    public void setSubscriptionTier(String subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getCallerToken() {
        return callerToken;
    }

    public void setCallerToken(String callerToken) {
        this.callerToken = callerToken;
    }

    public String getApplicationTier() {
        return applicationTier;
    }

    public void setApplicationTier(String applicationTier) {
        this.applicationTier = applicationTier;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public int getSpikeArrestLimit() {
        return spikeArrestLimit;
    }

    public void setSpikeArrestLimit(int spikeArrestLimit) {
        this.spikeArrestLimit = spikeArrestLimit;
    }

    public String getSubscriberTenantDomain() {
        return subscriberTenantDomain;
    }

    public void setSubscriberTenantDomain(String subscriberTenantDomain) {
        this.subscriberTenantDomain = subscriberTenantDomain;
    }

    public String getSpikeArrestUnit() {
        return spikeArrestUnit;
    }

    public void setSpikeArrestUnit(String spikeArrestUnit) {
        this.spikeArrestUnit = spikeArrestUnit;
    }

    public boolean isStopOnQuotaReach() {
        return stopOnQuotaReach;
    }

    public void setStopOnQuotaReach(boolean stopOnQuotaReach) {
        this.stopOnQuotaReach = stopOnQuotaReach;
    }

    public long getApplicationThrottleLimit() {
        return applicationThrottleLimit;
    }

    public void setApplicationThrottleLimit(long applicationThrottleLimit) {
        this.applicationThrottleLimit = applicationThrottleLimit;
    }
}
