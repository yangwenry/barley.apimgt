package barley.apimgt.impl.service;

import org.json.simple.JSONObject;

import barley.apimgt.api.APIConsumer;
import barley.apimgt.api.APIManagementException;
import barley.apimgt.impl.APIManagerFactory;

public class APIConsumerAdmin {
	

	public JSONObject resumeWorkflow(Object[] args, String username) throws APIManagementException {
		APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(username);
		return consumer.resumeWorkflow(args);
	}

}
