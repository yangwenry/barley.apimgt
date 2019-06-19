package barley.apimgt.impl.utils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import barley.apimgt.api.APIManagementException;

public class HttpUtils {
	
	private static final Log log = LogFactory.getLog(HttpUtils.class);

	public static void doPost(String endpoint, List<NameValuePair> urlParams) throws APIManagementException {
		HttpPost httpPost = null;
    	try {
			URL endpointURL = new URL(endpoint);
	        String endpointProtocol = endpointURL.getProtocol();
	        int endpointPort = endpointURL.getPort();
	
	        HttpClient endpointClient = APIUtil.getHttpClient(endpointPort, endpointProtocol);
	        
	        httpPost = new HttpPost(endpoint);
	        httpPost.setEntity(new UrlEncodedFormEntity(urlParams, "UTF-8"));
	
	        int statusCode;
	        HttpResponse httpResponse = endpointClient.execute(httpPost);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            
            log.info("http response statusCode:" + statusCode);
	        if (statusCode != HttpStatus.SC_OK) {
	            throw new IOException("failed : HTTP error code : " + statusCode);
	        } else {
	            if (log.isDebugEnabled()) {
	                log.debug("Successfully submitted request. HTTP status : 200");
	            }
	        }
		} catch (Exception e) {
			log.error("Error while " + endpoint + " - " + e.getMessage());
			handleException("Error while " + endpoint + " - " + e.getMessage(), e);
        } finally {
        	if(httpPost != null) httpPost.reset();
        }
    }
	
	public static String receive(String endpoint, List<NameValuePair> urlParams) throws APIManagementException {
		String responseStr = null;
		HttpPost httpPost = null;
    	try {
			URL endpointURL = new URL(endpoint);
	        String endpointProtocol = endpointURL.getProtocol();
	        int endpointPort = endpointURL.getPort();
	
	        HttpClient endpointClient = APIUtil.getHttpClient(endpointPort, endpointProtocol);
	        
	        httpPost = new HttpPost(endpoint);
	        httpPost.setEntity(new UrlEncodedFormEntity(urlParams, "UTF-8"));
	
	        int statusCode;
	        HttpResponse httpResponse = endpointClient.execute(httpPost);
            HttpEntity resEntity = httpResponse.getEntity();
            
            responseStr = EntityUtils.toString(resEntity);
            log.info("http response 결과:" + responseStr);
            
            statusCode = httpResponse.getStatusLine().getStatusCode();
            
            if(statusCode == HttpStatus.SC_NOT_FOUND) {
            	return null;
            } else if (statusCode != HttpStatus.SC_OK) {
                throw new IOException("Error occurred while calling endpoint: HTTP error code : " + statusCode);
            } else {
            	return responseStr;
            }
    	} catch (Exception e) {
    		log.error("Error while " + endpoint + " - " + e.getMessage());
    		handleException("Error while " + endpoint + " - " + e.getMessage(), e);
        } finally {
        	if(httpPost != null) httpPost.reset();
        }	
    	return responseStr;
    }
	
	private static void handleException(String msg, Exception e) throws APIManagementException {
        throw new APIManagementException(msg, e);
    }
	
}
