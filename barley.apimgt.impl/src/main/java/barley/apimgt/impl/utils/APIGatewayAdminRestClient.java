package barley.apimgt.impl.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.google.gson.Gson;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.model.APIIdentifier;
import barley.apimgt.impl.dto.Environment;
import barley.apimgt.impl.stub.APIData;
import barley.apimgt.impl.template.APITemplateBuilder;
import barley.apimgt.impl.template.APITemplateException;

/***
 * (추가) web client api 호출  
 * @author yangwenry
 *
 */
public class APIGatewayAdminRestClient extends AbstractAPIGatewayAdminClient {
	
	private static final Log log = LogFactory.getLog(APIGatewayAdminRestClient.class);

    private Environment environment;
    private String baseUrl;
    private String qualifiedName;
    private String qualifiedDefaultApiName;
    

    public APIGatewayAdminRestClient(APIIdentifier apiId, Environment environment) throws APIManagementException {
        this.qualifiedName = APIUtil.replaceEmailDomain(apiId.getProviderName()) + "--" + apiId.getApiName() + ":v" + apiId.getVersion();
        this.qualifiedDefaultApiName = APIUtil.replaceEmailDomain(apiId.getProviderName()) + "--" + apiId.getApiName();
        String providerDomain = apiId.getProviderName();
        providerDomain = APIUtil.replaceEmailDomainBack(providerDomain);
        this.environment = environment;
        this.baseUrl = this.environment.getApiServiceEndpoint() + "/api/apiMgt";
    }

    /**
     * Add the API to the gateway
     *
     * @param builder - APITemplateBuilder instance
     * @param tenantDomain - The Tenant Domain
     * @throws APITemplateException 
     * @throws AxisFault
     */
    public void addApi(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws APIManagementException {
    	String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	String apiConfig;
		try {
			apiConfig = builder.getConfigStringForTemplate(environment);
			
	        // Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiData", apiConfig));
	        
	        HttpUtils.doPost(this.baseUrl + "/addApi", urlParams);
		} catch (APITemplateException e) {
			String errorMsg = "Error while obtaining API information from gateway. ";
			throw new APIManagementException(errorMsg, e);
		} catch (APIManagementException e) {
            String errorMsg = "Error while obtaining WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
    }
    
    public void addPrototypeApiScriptImpl(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws APIManagementException {
    	String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	String apiConfig;
		try {
			apiConfig = builder.getConfigStringForPrototypeScriptAPI(environment);
			
	        // Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiData", apiConfig));
	        
	        HttpUtils.doPost(this.baseUrl + "/addApi", urlParams);
		} catch (APITemplateException e) {
			String errorMsg = "Error while obtaining API information from gateway. ";
			throw new APIManagementException(errorMsg, e);
		} catch (APIManagementException e) {
            String errorMsg = "Error while obtaining WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
	}
    
    public void addDefaultAPI(APITemplateBuilder builder, String tenantDomain, String defaultVersion, APIIdentifier apiId) throws APIManagementException {
    	String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	String apiConfig;
		try {
			apiConfig = builder.getConfigStringForDefaultAPITemplate(defaultVersion);
			
	        // Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiData", apiConfig));
	        
	        HttpUtils.doPost(this.baseUrl + "/addApi", urlParams);
		} catch (APITemplateException e) {
			String errorMsg = "Error while obtaining API information from gateway. ";
			throw new APIManagementException(errorMsg, e);
		} catch (APIManagementException e) {
            String errorMsg = "Error while obtaining WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
	}

    public void updateApi(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws APIManagementException {
    	String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	String apiConfig;
		try {
			apiConfig = builder.getConfigStringForTemplate(environment);
			
	        // Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiName", qualifiedName));
	        urlParams.add(new BasicNameValuePair("apiData", apiConfig));
	        
	        HttpUtils.doPost(this.baseUrl + "/updateApi", urlParams);
		} catch (APITemplateException e) {
			String errorMsg = "Error while update API information from gateway. ";
			throw new APIManagementException(errorMsg, e);
		} catch (APIManagementException e) {
            String errorMsg = "Error while update WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
	}
    
    public void updateApiForInlineScript(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws APIManagementException {
    	String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	String apiConfig;
		try {
			apiConfig = builder.getConfigStringForPrototypeScriptAPI(environment);
			
	        // Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiName", qualifiedName));
	        urlParams.add(new BasicNameValuePair("apiData", apiConfig));
	        
	        HttpUtils.doPost(this.baseUrl + "/updateApi", urlParams);
		} catch (APITemplateException e) {
			String errorMsg = "Error while update API information from gateway. ";
			throw new APIManagementException(errorMsg, e);
		} catch (APIManagementException e) {
            String errorMsg = "Error while update WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
	}
    
    public void updateDefaultApi(APITemplateBuilder builder, String tenantDomain, String defaultVersion, APIIdentifier apiId) throws APIManagementException {
    	String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	String apiConfig;
		try {
			apiConfig = builder.getConfigStringForDefaultAPITemplate(defaultVersion);
			
	        // Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiName", qualifiedDefaultApiName));
	        urlParams.add(new BasicNameValuePair("apiData", apiConfig));
	        
	        HttpUtils.doPost(this.baseUrl + "/updateApi", urlParams);
		} catch (APITemplateException e) {
			String errorMsg = "Error while update API information from gateway. ";
			throw new APIManagementException(errorMsg, e);
		} catch (APIManagementException e) {
            String errorMsg = "Error while update WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
	}

	public void deleteApi(String tenantDomain, APIIdentifier apiId) throws APIManagementException {
		String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	try {
			// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiName", qualifiedName));
	        
	        HttpUtils.doPost(this.baseUrl + "/deleteApi", urlParams);
		} catch (APIManagementException e) {
            String errorMsg = "Error while delete WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
	}
	
	public void deleteDefaultApi(String tenantDomain, APIIdentifier apiId) throws APIManagementException {
		String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	try {
			// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiName", qualifiedDefaultApiName));
	        
	        HttpUtils.doPost(this.baseUrl + "/deleteApi", urlParams);
		} catch (APIManagementException e) {
            String errorMsg = "Error while delete WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
	}
	
	/**
     * Get API from the gateway
     *
     * @param tenantDomain - The Tenant Domain
     * @return - An APIData instance
	 * @throws APIManagementException 
     * @throws AxisFault
     */
    public APIData getApi(String tenantDomain, APIIdentifier apiId) throws APIManagementException {
    	String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	APIData apiData = null;
    	String entityStr;
    	try {
	    	// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiName", qualifiedName));
	        
	        entityStr = HttpUtils.receive(this.baseUrl + "/getApiByName", urlParams);   
	        if(entityStr != null) {
	        	apiData = new Gson().fromJson(entityStr, APIData.class);
            }
    	} catch (APIManagementException e) {
            String errorMsg = "Error while obtaining WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
    	return apiData;
    }
	
	public APIData getDefaultApi(String tenantDomain, APIIdentifier apiId) throws APIManagementException {
		String apiName = apiId.getApiName();
    	String apiVersion = apiId.getVersion();
    	APIData apiData = null;
    	String entityStr;
    	try {
	    	// Request parameters.
	        List<NameValuePair> urlParams = new ArrayList<NameValuePair>(0);
	        urlParams.add(new BasicNameValuePair("apiName", qualifiedDefaultApiName));
	        
	        entityStr = HttpUtils.receive(this.baseUrl + "/getApiByName", urlParams);   
	        if(entityStr != null) {
	        	apiData = new Gson().fromJson(entityStr, APIData.class);
            }
    	} catch (APIManagementException e) {
            String errorMsg = "Error while obtaining WebApi information from gateway. Api Name : " + apiName + " Api " +
                    "Version : " + apiVersion;
            throw new APIManagementException(errorMsg, e);
        }
    	return apiData;
	}

	
}

