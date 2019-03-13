/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package barley.apimgt.impl.clients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.wso2.carbon.apimgt.tier.cache.stub.TierCacheServiceStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.service.TierCacheService;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.impl.utils.HttpUtils;

public class TierCacheInvalidationClient {
    private static final Log log = LogFactory.getLog(TierCacheInvalidationClient.class);
    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;
    
    String storeServerURL;
    
    String storeUserName;
    
    String storePassword;

    public TierCacheInvalidationClient() throws APIManagementException {
        APIManagerConfiguration config =
                                         ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                                               .getAPIManagerConfiguration();
        storeServerURL = config.getFirstProperty(APIConstants.API_STORE_SERVER_URL);
        storeUserName = config.getFirstProperty(APIConstants.API_STORE_USERNAME);
        storePassword = config.getFirstProperty(APIConstants.API_STORE_PASSWORD);
    }

    public void clearCaches(String tenantDomain) {
        String cookie;

        // Clear Store Cache
        try {
        	// (임시주석)
            //cookie = login(storeServerURL, storeUserName, storePassword);
        	cookie = null;
            clearCache(tenantDomain, storeServerURL, cookie);
        } catch (APIManagementException e) {
            log.error("Error while invalidating the tier cache in Store for tenant : " + tenantDomain, e);
        }
        
    }

    public void clearCache(String tenantDomain, String serverURL, String cookie) throws APIManagementException {
    	/* (주석) 웹서비스를 restful으로 변경
        TierCacheServiceStub tierCacheServiceStub;

        ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        tierCacheServiceStub = new TierCacheServiceStub(ctx, serverURL + "TierCacheService");
        ServiceClient client = tierCacheServiceStub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
        options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);

        tierCacheServiceStub.invalidateCache(tenantDomain);
        */
    	
    	String endpoint = serverURL + "/api/invalidateTierCache";
    	try {
			// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("tenantDomain", tenantDomain));
	        
	        HttpUtils.doPost(endpoint, urlParams);
		} catch (APIManagementException e) {
            String errorMsg = "Error while invalidate Tier Cache.";
            throw new APIManagementException(errorMsg, e);
        }
    }

    private String login(String serverURL, String userName, String password) throws AxisFault {
        if (serverURL == null || userName == null || password == null) {
            throw new AxisFault("Required admin configuration unspecified");
        }

        String host;
        try {
            host = new URL(serverURL).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("Server URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, serverURL + "AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(userName, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating ", e);
        }
    }
    
    private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }

}
