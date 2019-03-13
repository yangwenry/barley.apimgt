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
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.dto.Environment;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.stub.keymgt.OAuthApplicationInfo;

/**
 * (추가) 2017.09.26 
 * @author yangwenry
 *
 */
public class KeyMgtProviderRestClient {

	private String baseUrl;
    private static final Log log = LogFactory.getLog(KeyMgtProviderRestClient.class);
    
    public KeyMgtProviderRestClient() {
    	APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    	Map<String, Environment> environments = config.getApiGatewayEnvironments();
    	Environment environment = null;
    	for(String key : environments.keySet()) {
    		environment = environments.get(key);
    	}
    	this.baseUrl = environment.getApiServiceEndpoint() + "/api/apiKeyMgt";
    }
    
    public void removeScopeCache(String[] consumerKeys) throws APIManagementException {
    	try {
			// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        if(consumerKeys != null) urlParams.add(new BasicNameValuePair("consumerKeys", String.join(",", consumerKeys)));
	        
	        HttpUtils.doPost(this.baseUrl + "/removeScopeCache", urlParams);
		} catch (APIManagementException e) {
            String errorMsg = "Error while invalidate removeScopeCache.";
            throw new APIManagementException(errorMsg, e);
        }
	}

    
}
