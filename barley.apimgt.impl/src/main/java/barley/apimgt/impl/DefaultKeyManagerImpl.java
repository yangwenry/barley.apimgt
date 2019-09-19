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

package barley.apimgt.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.json.JSONException;
import org.json.JSONObject;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.model.API;
import barley.apimgt.api.model.AccessTokenInfo;
import barley.apimgt.api.model.AccessTokenRequest;
import barley.apimgt.api.model.ApplicationConstants;
import barley.apimgt.api.model.KeyManagerConfiguration;
import barley.apimgt.api.model.OAuthAppRequest;
import barley.apimgt.api.model.OAuthApplicationInfo;
import barley.apimgt.impl.dao.ApiMgtDAO;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.impl.utils.KeyManagerRestClient;
import barley.core.utils.BarleyUtils;
import barley.identity.core.util.IdentityConfigParser;
import barley.identity.core.util.IdentityCoreConstants;
import barley.identity.oauth.common.OAuthConstants;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class DefaultKeyManagerImpl extends AbstractKeyManager {

	private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    private static final String GRANT_TYPE_VALUE = "client_credentials";
    private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";
    private static final String CONFIG_ELEM_OAUTH = "OAuth";

    private KeyManagerConfiguration configuration;

    private static final Log log = LogFactory.getLog(DefaultKeyManagerImpl.class);
    
    @Override
	public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {
    	// OAuthApplications are created by calling to APIKeyMgtSubscriber Service
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();

        // Subscriber's name should be passed as a parameter, since it's under the subscriber the OAuth App is created.
        String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.
                OAUTH_CLIENT_USERNAME);
        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.APP_KEY_TYPE);
        if (keyType != null) {
            applicationName = applicationName + "_" + keyType;
        }

        if (log.isDebugEnabled()) {
            log.debug("Trying to create OAuth application :" + applicationName);
        }
        

        String tokenScope = (String) oAuthApplicationInfo.getParameter("tokenScope");
        String tokenScopes[] = new String[1];
        tokenScopes[0] = tokenScope;

        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
        barley.apimgt.impl.stub.keymgt.OAuthApplicationInfo info = null;
        try {
        	barley.apimgt.impl.stub.keymgt.OAuthApplicationInfo applicationToCreate = new barley.apimgt.impl.stub.keymgt.OAuthApplicationInfo();
            applicationToCreate.setIsSaasApplication(oAuthApplicationInfo.getIsSaasApplication());
            applicationToCreate.setCallBackURL(oAuthApplicationInfo.getCallBackURL());
            applicationToCreate.setClientName(applicationName);
            applicationToCreate.setAppOwner(userId);
            
            KeyManagerRestClient keyMgtClient = new KeyManagerRestClient();
            info = keyMgtClient.createOAuthApplicationbyApplicationInfo(applicationToCreate);
        } catch (Exception e) {
            handleException("Can not create OAuth application  : " + applicationName, e);
        } finally {
            
        }

        if (info == null || info.getJsonString() == null) {
            handleException("OAuth app does not contains required data  : " + applicationName,
                    new APIManagementException("OAuth app does not contains required data"));
        }

        oAuthApplicationInfo.setClientName(info.getClientName());
        oAuthApplicationInfo.setClientId(info.getClientId());
        oAuthApplicationInfo.setCallBackURL(info.getCallBackURL());
        oAuthApplicationInfo.setClientSecret(info.getClientSecret());
        oAuthApplicationInfo.setIsSaasApplication(info.getIsSaasApplication());

        try {
            JSONObject jsonObject = new JSONObject(info.getJsonString());

            if (jsonObject.has(ApplicationConstants.
                    OAUTH_REDIRECT_URIS)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_REDIRECT_URIS, jsonObject.get(ApplicationConstants.OAUTH_REDIRECT_URIS));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_NAME)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_NAME, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_NAME));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_GRANT)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_GRANT, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_GRANT));
            }
        } catch (JSONException e) {
            handleException("Can not retrieve information of the created OAuth application", e);
        }

        return oAuthApplicationInfo;
	}

	@Override
	public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws APIManagementException {
		OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getOAuthApplicationInfo();
		KeyManagerRestClient keyMgtClient = null;

        try {
            keyMgtClient = new KeyManagerRestClient();

            String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME);
            String[] grantTypes = null;
            if (oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT) != null) {
                grantTypes = ((String)oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT))
                                                                                                           .split(",");
            }
            String applicationName = oAuthApplicationInfo.getClientName();
            String keyType = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.APP_KEY_TYPE);

            if (keyType != null) {
                applicationName = applicationName + "_" + keyType;
            }
            log.debug("Updating OAuth Client with ID : " + oAuthApplicationInfo.getClientId());

            if (log.isDebugEnabled() && oAuthApplicationInfo.getCallBackURL() != null) {
                log.debug("CallBackURL : " + oAuthApplicationInfo.getCallBackURL());
            }

            if (log.isDebugEnabled() && applicationName != null) {
                log.debug("Client Name : " + applicationName);
            }
            
            barley.apimgt.impl.stub.keymgt.OAuthApplicationInfo applicationInfo 
            			= keyMgtClient.updateOAuthApplication(userId, applicationName, oAuthApplicationInfo.getCallBackURL(),
            								oAuthApplicationInfo.getClientId(), grantTypes);
            
            OAuthApplicationInfo newAppInfo = new OAuthApplicationInfo();
            newAppInfo.setClientId(applicationInfo.getClientId());
            newAppInfo.setCallBackURL(applicationInfo.getCallBackURL());
            newAppInfo.setClientSecret(applicationInfo.getClientSecret());

            return newAppInfo;
        } catch (Exception e) {
            handleException("Error occurred while updating OAuth Client : ", e);
        } finally {
            
        }

        return null;
	}

	@Override
	public void deleteApplication(String consumerKey) throws APIManagementException {
		if (log.isDebugEnabled()) {
            log.debug("Trying to delete OAuth application for consumer key :" + consumerKey);
        }

		KeyManagerRestClient keyMgtClient = null;
        try {
            keyMgtClient = new KeyManagerRestClient();
            keyMgtClient.deleteOAuthApplication(consumerKey);
        } catch (Exception e) {
            handleException("Can not remove service provider for the given consumer key : " + consumerKey, e);
        } finally {
            
        }
	}

	@Override
	public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {
		//SubscriberKeyMgtClient keyMgtClient = APIUtil.getKeyManagementClient();

		KeyManagerRestClient keyMgtClient = null;
        if (log.isDebugEnabled()) {
            log.debug("Trying to retrieve OAuth application for consumer key :" + consumerKey);
        }

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        try {
            keyMgtClient = new KeyManagerRestClient();
            barley.apimgt.impl.stub.keymgt.OAuthApplicationInfo info = keyMgtClient.getOAuthApplication(consumerKey);

            if (info == null || info.getClientId() == null) {
                return null;
            }
            oAuthApplicationInfo.setClientName(info.getClientName());
            oAuthApplicationInfo.setClientId(info.getClientId());
            oAuthApplicationInfo.setCallBackURL(info.getCallBackURL());
            oAuthApplicationInfo.setClientSecret(info.getClientSecret());

            JSONObject jsonObject = new JSONObject(info.getJsonString());

            if (jsonObject.has(ApplicationConstants.
                    OAUTH_REDIRECT_URIS)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_REDIRECT_URIS, jsonObject.get(ApplicationConstants.OAUTH_REDIRECT_URIS));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_NAME)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_NAME, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_NAME));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_GRANT)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_GRANT, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_GRANT));
            }

        } catch (Exception e) {
            handleException("Can not retrieve OAuth application for the given consumer key : " + consumerKey, e);
        } finally {
            
        }
        return oAuthApplicationInfo;
	}

	@Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest)
            throws APIManagementException {

        String newAccessToken;
        long validityPeriod;
        AccessTokenInfo tokenInfo = null;

        if (tokenRequest == null) {
            log.warn("No information available to generate Token.");
            return null;
        }

        String tokenEndpoint = configuration.getParameter(APIConstants.TOKEN_URL);
        //To revoke tokens we should call revoke API deployed in API gateway.
        String revokeEndpoint = configuration.getParameter(APIConstants.REVOKE_URL);
        URL keyMgtURL = new URL(tokenEndpoint);
        int keyMgtPort = keyMgtURL.getPort();
        String keyMgtProtocol = keyMgtURL.getProtocol();

        // Call the /revoke only if there's a token to be revoked.
        try {
            if (tokenRequest.getTokenToRevoke() != null && !"".equals(tokenRequest.getTokenToRevoke())) {
                URL revokeEndpointURL = new URL(revokeEndpoint);
                String revokeEndpointProtocol = revokeEndpointURL.getProtocol();
                int revokeEndpointPort = revokeEndpointURL.getPort();

                HttpClient revokeEPClient = APIUtil.getHttpClient(revokeEndpointPort, revokeEndpointProtocol);

                HttpPost httpRevokePost = new HttpPost(revokeEndpoint);

                // Request parameters.
                List<NameValuePair> revokeParams = new ArrayList<NameValuePair>(3);
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
                revokeParams.add(new BasicNameValuePair("token", tokenRequest.getTokenToRevoke()));


                //Revoke the Old Access Token
                httpRevokePost.setEntity(new UrlEncodedFormEntity(revokeParams, "UTF-8"));
                int statusCode;
                try {
                    HttpResponse revokeResponse = revokeEPClient.execute(httpRevokePost);
                    statusCode = revokeResponse.getStatusLine().getStatusCode();
                } finally {
                    httpRevokePost.reset();
                }

                if (statusCode != 200) {
                    throw new RuntimeException("Token revoke failed : HTTP error code : " + statusCode);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully submitted revoke request for old application token. HTTP status : 200");
                    }
                }
            }
            //get default application access token name from config.

            String applicationTokenScope = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                                            getAPIManagerConfiguration().getFirstProperty(APIConstants
                                                                            .APPLICATION_TOKEN_SCOPE);

            // When validity time set to a negative value, a token is considered never to expire.
            if (tokenRequest.getValidityPeriod() == OAuthConstants.UNASSIGNED_VALIDITY_PERIOD) {
                // Setting a different -ve value if the set value is -1 (-1 will be ignored by TokenValidator)
                tokenRequest.setValidityPeriod(-2);
            }

            //Generate New Access Token
            HttpClient tokenEPClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpPost httpTokpost = new HttpPost(tokenEndpoint);
            List<NameValuePair> tokParams = new ArrayList<NameValuePair>(3);
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_GRANT_TYPE, GRANT_TYPE_VALUE));
            tokParams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_VALIDITY,
                    Long.toString(tokenRequest.getValidityPeriod())));
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
            StringBuilder builder = new StringBuilder();
            builder.append(applicationTokenScope);

            for (String scope : tokenRequest.getScope()) {
                builder.append(' ').append(scope);
            }

            tokParams.add(new BasicNameValuePair("scope", builder.toString()));

            httpTokpost.setEntity(new UrlEncodedFormEntity(tokParams, "UTF-8"));
            try {
                HttpResponse tokResponse = tokenEPClient.execute(httpTokpost);
                HttpEntity tokEntity = tokResponse.getEntity();
                
                String responseStr = EntityUtils.toString(tokEntity);
                JSONObject responseObj = new JSONObject(responseStr);
                
                if (tokResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Error occurred while calling token endpoint: HTTP error code : " +
                    		responseObj.getString("statusType"));
                } else {
                    tokenInfo = new AccessTokenInfo();
                    newAccessToken = responseObj.get(OAUTH_RESPONSE_ACCESSTOKEN).toString();
                    validityPeriod = Long.parseLong(responseObj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());
                    if (responseObj.has("scope")) {
                        tokenInfo.setScope(((String) responseObj.get("scope")).split(" "));
                    }
                    tokenInfo.setAccessToken(newAccessToken);
                    tokenInfo.setValidityPeriod(validityPeriod);
                }
            } finally {
                httpTokpost.reset();
            }
        } catch (ClientProtocolException e) {
            handleException("Error while creating token - Invalid protocol used", e);
        } catch (UnsupportedEncodingException e) {
            handleException("Error while preparing request for token/revoke APIs", e);
        } catch (IOException e) {
            handleException("Error while creating tokens - " + e.getMessage(), e);
        } catch (JSONException e) {
            handleException("Error while parsing response from token api", e);
        }

        return tokenInfo;
    }

	@Override
	public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest) throws APIManagementException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException {
        if (configuration != null) {
            this.configuration = configuration;
        } else {

            // If the provided configuration is null, read the Server-URL and other properties from
            // APIKeyValidator section.
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();
            /**
             * we need to read identity.xml here because we need to get default validity time for access_token in order
             * to set in semi-manual.
             */
            IdentityConfigParser configParser;
            
            // (수정) 2018.08.14 - identity.xml 파일 위치를 변경
            //String configFile = BarleyUtils.getCarbonConfigDirPath() + File.separator + "identity.xml";
            String configFile = BarleyUtils.getCarbonConfigDirPath() + File.separator + "identity" + File.separator + "identity.xml";
            configParser = IdentityConfigParser.getInstance(configFile);
            OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

            String validityPeriod = null;

            if (oauthElem != null) {
                if (log.isDebugEnabled()) {
                    log.debug("identity configs have loaded. ");
                }
                // Primary/Secondary supported login mechanisms
                OMElement loginConfigElem =  oauthElem.getFirstChildWithName(getQNameWithIdentityNS("AccessTokenDefaultValidityPeriod"));
                validityPeriod = loginConfigElem.getText();
            }

            if (this.configuration == null) {
                this.configuration = new KeyManagerConfiguration();
                this.configuration.setManualModeSupported(true);
                this.configuration.setResourceRegistrationEnabled(true);
                this.configuration.setTokenValidityConfigurable(true);
                this.configuration.addParameter(APIConstants.AUTHSERVER_URL, config.getFirstProperty(APIConstants
                        .KEYMANAGER_SERVERURL));
                this.configuration.addParameter(APIConstants.KEY_MANAGER_USERNAME, config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME));
                this.configuration.addParameter(APIConstants.KEY_MANAGER_PASSWORD, config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD))
                ;
                this.configuration.addParameter(APIConstants.REVOKE_URL, config.getFirstProperty(APIConstants
                        .REVOKE_API_URL));
                this.configuration.addParameter(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD, validityPeriod);
                String revokeUrl = config.getFirstProperty(APIConstants.REVOKE_API_URL);

                // Read the revoke url and replace revoke part to get token url.
                String tokenUrl = revokeUrl != null ? revokeUrl.replace("revoke", "token") : null;
                this.configuration.addParameter(APIConstants.TOKEN_URL, tokenUrl);
            }
        }

        // (임시주석) 
        //SubscriberKeyMgtClientPool.getInstance().setConfiguration(this.configuration);

    }

    private QName getQNameWithIdentityNS(String localPart) {
        return new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, localPart);
    }
    
    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {
        return configuration;
    }
    
    @Override
    public OAuthApplicationInfo buildFromJSON(String jsonInput) throws APIManagementException {
        return null;
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws APIManagementException {
//        //Register new resource means create new API with given Scopes.
        //todo commented below code because of blocker due to API publish fail. need to find a better way of doing this
//        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
//        apiMgtDAO.addAPI(api, CarbonContext.getThreadLocalCarbonContext().getTenantId());

        return true;
    }

    @Override
    public Map getResourceByApiId(String apiId) throws APIManagementException {
        return null;
    }

    @Override
    public boolean updateRegisteredResource(API api, Map resourceAttributes) throws APIManagementException {
        return false;
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String apiID) throws APIManagementException {

    }

    @Override
    public void deleteMappedApplication(String consumerKey) throws APIManagementException {

    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String consumerKey) throws APIManagementException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return apiMgtDAO.getActiveTokensOfConsumerKey(consumerKey);
    }

    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws APIManagementException {
        return null;
    }

    /**
     * common method to throw exceptions.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }

	

}
