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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.RegistryConstants;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.dto.Environment;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.HttpGatewayUtils;

/**
 * This is the client implementation of RegistryCacheInvalidationService
 *
 */
public class RegistryCacheInvalidationClient {
    private static final Log log = LogFactory.getLog(RegistryCacheInvalidationClient.class);
    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    Map<String, Environment> environments;
    

    public RegistryCacheInvalidationClient() throws APIManagementException {
        APIManagerConfiguration config =
                                         ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                                               .getAPIManagerConfiguration();
        environments = config.getApiGatewayEnvironments();
    }

    
    /**
     * Invalidates the registry cache for tiers.xml of given tenant domain
     * @param tenantDomain
     * @throws APIManagementException
     */
    public void clearTiersResourceCache(String tenantDomain) throws APIManagementException {
        String cookie;

        // Clear Gateway Cache
        try {
            String gatewayServerURL;
            for (Map.Entry<String, Environment> entry : environments.entrySet()) {
                Environment environment = entry.getValue();
                gatewayServerURL = environment.getServerURL();
                // (????????????)
                //cookie = login(gatewayServerURL, environment.getUserName(), environment.getPassword());
                cookie = null;
                clearCache(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH 
                           + APIConstants.API_TIER_LOCATION, tenantDomain, gatewayServerURL, cookie);                
            }
        } catch (APIManagementException e) {
            log.error("Error while invalidating the tiers.xml cache in gateway for tenant : " + tenantDomain, e);
        }
        
    }
    
    /**
     * Invalidates registry cache of the resource in the given path in given server
     * @param path registry path of the resource
     * @param tenantDomain
     * @param serverURL
     * @param cookie
     * @throws AxisFault
     * @throws RemoteException
     * @throws APIManagementException
     */
    public void clearCache(String path, String tenantDomain, String serverURL, String cookie) throws APIManagementException {
    	/* (??????) ??????????????? restful?????? ??????
        RegistryCacheInvalidationServiceStub registryCacheServiceStub;

        ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        registryCacheServiceStub = 
                new RegistryCacheInvalidationServiceStub(ctx, serverURL + "RegistryCacheInvalidationService");
        ServiceClient client = registryCacheServiceStub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
        options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        
        try {
            registryCacheServiceStub.invalidateCache(path, tenantDomain);      
        } catch (RegistryCacheInvalidationServiceAPIManagementExceptionException e) {
            APIUtil.handleException(e.getMessage(), e);
        }
        */
    	
    	String endpoint = serverURL + "/invalidateRegistryCache";
    	try {
			// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("path", path));
	        urlParams.add(new BasicNameValuePair("tenantDomain", tenantDomain));
	        
	        HttpGatewayUtils.doPost(endpoint, urlParams);
		} catch (APIManagementException e) {
            String errorMsg = "Error while invalidate Registry Cache.";
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Login with given credentials and returns cookie
     * @param serverURL
     * @param userName
     * @param password
     * @return
     * @throws AxisFault
     */
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
