package barley.apimgt.impl.utils;

import java.io.File;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.core.utils.BarleyUtils;

/**
 * RemoteUserStroeManager Admin service client.
 * 
 */
public class RemoteUserManagerClient {
	private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;
	private RemoteUserStoreManagerServiceStub userStoreManagerStub;

	public RemoteUserManagerClient(String cookie) throws APIManagementException {

		APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();
		String serviceURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
		String username = config.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME);
		String password = config.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD);
		if (serviceURL == null || username == null || password == null) {
			throw new APIManagementException("Required connection details for authentication");
		}
		
		try {

			String clientRepo = BarleyUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "deployment" + File.separator + "client";
			String clientAxisConf = BarleyUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2"+ File.separator +"axis2_client.xml";
			
			ConfigurationContext configContext = ConfigurationContextFactory. createConfigurationContextFromFileSystem(clientRepo,clientAxisConf);
			userStoreManagerStub = new RemoteUserStoreManagerServiceStub(configContext, serviceURL +
			                                                                   "RemoteUserStoreManagerService");
			ServiceClient svcClient = userStoreManagerStub._getServiceClient();
			BarleyUtils.setBasicAccessSecurityHeaders(username, password, true,svcClient);
			Options options = svcClient.getOptions();
			options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
			options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
			options.setCallTransportCleanup(true);
			options.setManageSession(true);		
			options.setProperty(HTTPConstants.COOKIE_STRING, cookie);	
		
		} catch (AxisFault axisFault) {
			throw new APIManagementException(
			                                 "Error while initializing the remote user store manager stub",
			                                 axisFault);
		}
	}

	/**
	 * Return userlist based on a claim
	 * 
	 * @param claim - The claim
	 * @param claimValue - The Claim Value
	 * @return - A user list
	 * @throws APIManagementException
	 */
	public String[] getUserList(String claim, String claimValue) throws APIManagementException {
		try {
			return userStoreManagerStub.getUserList(claim, claimValue, null);
		} catch (Exception e) {
			throw new APIManagementException("Error when retrieving user list", e);
		}
	
	}
}
