package barley.apimgt.impl.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.dto.Environment;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.stub.APIData;
import barley.apimgt.impl.stub.keymgt.OAuthApplicationInfo;

/**
 * (추가) 2017.09.26 
 * @author yangwenry
 *
 */
public class KeyManagerRestClient {

    private String baseUrl;
    
    private static final Log log = LogFactory.getLog(KeyManagerRestClient.class);
    
    public KeyManagerRestClient() {
    	APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    	Map<String, Environment> environments = config.getApiGatewayEnvironments();
    	Environment environment = null;
    	for(String key : environments.keySet()) {
    		environment = environments.get(key);
    	}
    	this.baseUrl = environment.getApiServiceEndpoint() + "/api/keyManager";
    }
    
    public OAuthApplicationInfo createOAuthApplicationbyApplicationInfo(OAuthApplicationInfo applicationToCreate) throws APIManagementException {
    	String entityStr;
    	OAuthApplicationInfo info = null;
    	try {
    		// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        /*
	        ObjectMapper m = new ObjectMapper();
	        Map<String, Object> mappedObject = m.convertValue(applicationToCreate, Map.class);
	        for (Map.Entry<String, Object> entry : mappedObject.entrySet()) {
	        	if(entry.getValue() != null) urlParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
	        }
	        */
	        urlParams.add(new BasicNameValuePair("clientName", applicationToCreate.getClientName()));
	        urlParams.add(new BasicNameValuePair("callBackURL", applicationToCreate.getCallBackURL()));
	        urlParams.add(new BasicNameValuePair("clientSecret", applicationToCreate.getClientSecret()));
	        urlParams.add(new BasicNameValuePair("isSaasApplication", String.valueOf(applicationToCreate.getIsSaasApplication())));
	        urlParams.add(new BasicNameValuePair("appOwner", applicationToCreate.getAppOwner()));
	        
	        entityStr = HttpUtils.receive(this.baseUrl + "/createOAuthApp", urlParams);   
	        if(entityStr != null) {
	        	info = new Gson().fromJson(entityStr, OAuthApplicationInfo.class);
            }
    	} catch (APIManagementException e) {
            String errorMsg = "Error while parsing response from createOAuthApp";
            throw new APIManagementException(errorMsg, e);
        }
    	return info;
	}

	public OAuthApplicationInfo updateOAuthApplication(String userId,
			String applicationName, String callbackUrl, String consumerKey, String[] grantTypes) throws APIManagementException {
		String entityStr;
    	OAuthApplicationInfo info = null;
    	try {
    		// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("userId", userId));
	        urlParams.add(new BasicNameValuePair("applicationName", applicationName));
	        urlParams.add(new BasicNameValuePair("callbackUrl", callbackUrl));
	        urlParams.add(new BasicNameValuePair("consumerKey", consumerKey));
	        if(grantTypes != null) urlParams.add(new BasicNameValuePair("grantTypes", String.join(",", grantTypes)));
	        
	        entityStr = HttpUtils.receive(this.baseUrl + "/updateOAuthApp", urlParams);   
	        if(entityStr != null) {
	        	info = new Gson().fromJson(entityStr, OAuthApplicationInfo.class);
            }
    	} catch (APIManagementException e) {
            String errorMsg = "Error while parsing response from updateOAuthApp";
            throw new APIManagementException(errorMsg, e);
        }
    	return info;
	}

	public void deleteOAuthApplication(String consumerKey) throws APIManagementException {
		try {
			// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("consumerKey", consumerKey));
	        
	        HttpUtils.doPost(this.baseUrl + "/deleteOAuthApp", urlParams);
		} catch (APIManagementException e) {
            String errorMsg = "Error while invalidate deleteOAuthApplication.";
            throw new APIManagementException(errorMsg, e);
        }
	}

	public OAuthApplicationInfo getOAuthApplication(String consumerKey) throws APIManagementException {
		String entityStr;
    	OAuthApplicationInfo info = null;
    	try {
    		// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("consumerKey", consumerKey));
	        
	        entityStr = HttpUtils.receive(this.baseUrl + "/retrieveOAuthApp", urlParams);   
	        if(entityStr != null) {
	        	info = new Gson().fromJson(entityStr, OAuthApplicationInfo.class);
            }
    	} catch (APIManagementException e) {
            String errorMsg = "Error while parsing response from retrieveOAuthApp";
            throw new APIManagementException(errorMsg, e);
        }
    	return info;
	}

	
    
}
