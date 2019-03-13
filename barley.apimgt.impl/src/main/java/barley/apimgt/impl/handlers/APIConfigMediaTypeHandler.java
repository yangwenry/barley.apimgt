package barley.apimgt.impl.handlers;

import javax.cache.Cache;
import javax.cache.Caching;

import barley.apimgt.impl.APIConstants;
import barley.core.context.PrivilegedBarleyContext;
import barley.registry.core.jdbc.handlers.Handler;
import barley.registry.core.jdbc.handlers.RequestContext;

public class APIConfigMediaTypeHandler extends Handler {

    public void put(RequestContext requestContext) {
        clearConfigCache();
    }

    public void delete(RequestContext requestContext) {
        clearConfigCache();
    }

    private void clearConfigCache() {
    	// (수정) 2017.08.17 캐쉬 라이브러리 변경으로 인해 수정함.
        Cache workflowCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
//    	Cache workflowCache = Caching.getCachingProvider().getCacheManager().
                getCache(APIConstants.WORKFLOW_CACHE_NAME);
        String tenantDomain = PrivilegedBarleyContext.getThreadLocalCarbonContext().getTenantDomain();
        String cacheName = tenantDomain + "_" + APIConstants.WORKFLOW_CACHE_NAME;
        if (workflowCache.containsKey(cacheName)) {
            workflowCache.remove(cacheName);
        }

    }
}
