package barley.apimgt.impl.template;

import org.apache.velocity.VelocityContext;

import barley.apimgt.api.model.API;
import barley.apimgt.impl.APIConstants;

/**
 * Set if response caching enabled or not
 */
public class ResponseCacheConfigContext extends ConfigContextDecorator {
	
	private API api;

	public ResponseCacheConfigContext(ConfigContext context, API api) {
		super(context);
		this.api = api;
	}
	
	public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        // (임시주석) 2017.11.13 - 일단은 캐싱을 사용하지 않는다. org.wso2.caching.digest.REQUESTHASHGenerator 라이브러리를 찾을 수 없음.
        /*
        if (APIConstants.ENABLED.equalsIgnoreCase(api.getResponseCache())) {
            context.put("responseCacheEnabled", Boolean.TRUE);
            context.put("responseCacheTimeOut", api.getCacheTimeout());
        } else {
            context.put("responseCacheEnabled", Boolean.FALSE);
        }
        */
        context.put("responseCacheEnabled", Boolean.FALSE);

        return context;
    }

}
