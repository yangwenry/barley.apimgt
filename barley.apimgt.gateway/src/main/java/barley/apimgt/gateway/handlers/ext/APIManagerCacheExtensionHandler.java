package barley.apimgt.gateway.handlers.ext;

import java.util.Map;
import java.util.TreeMap;

import javax.cache.Caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;

import barley.apimgt.gateway.APIMgtGatewayConstants;
import barley.apimgt.impl.APIConstants;
import barley.core.MultitenantConstants;
import barley.core.context.PrivilegedBarleyContext;
import barley.registry.core.RegistryConstants;

/**
 * A simple extension handler to clear cache entries associated with the token when token revoked and refreshed.
 * /revoke api is there to revoke access tokens and it will call to key management server's oauth component and
 * revokes token and clear oauth cache. But there can be validation information objects
 * cached at gateway which associated with that token. So this handler will remove them form the cache.
 */
public class APIManagerCacheExtensionHandler extends AbstractHandler {

    private static final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
    private static final String DIRECTION_OUT = "Out";
    private static final Log log = LogFactory.getLog(APIManagerCacheExtensionHandler.class);

    public boolean mediate(MessageContext messageContext, String direction) {
        // In order to avoid a remote registry call occurring on each invocation, we
        // directly get the extension sequences from the local registry.
        Map localRegistry = messageContext.getConfiguration().getLocalRegistry();

        Object sequence = localRegistry.get(EXT_SEQUENCE_PREFIX + direction);
        if (sequence instanceof Mediator) {
            if (!((Mediator) sequence).mediate(messageContext)) {
                return false;
            }
        }

        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        sequence = localRegistry.get(apiName + "--" + direction);
        if (sequence instanceof Mediator) {
            return ((Mediator) sequence).mediate(messageContext);
        }
        return true;
    }

    private void clearCacheForAccessToken(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        TreeMap transportHeaders =
                ((TreeMap) axisMC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        String revokedToken = (String) transportHeaders.get(APIMgtGatewayConstants.REVOKED_ACCESS_TOKEN);
        String renewedToken = (String) transportHeaders.get(APIMgtGatewayConstants.DEACTIVATED_ACCESS_TOKEN);
        if (revokedToken != null) {

            //Find the actual tenant domain on which the access token was cached. It is stored as a reference in
            //the super tenant cache.
            String cachedTenantDomain = 
            		// (수정)
            		(String) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
            			getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME).get(revokedToken);
//            		Caching.getCachingProvider(RegistryConstants.CACHING_PROVIDER).getCacheManager().
//                    getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME, String.class, String.class).get(revokedToken);

            //Remove the super tenant cache entry.
            // (수정)
//            Caching.getCachingProvider(RegistryConstants.CACHING_PROVIDER).getCacheManager().
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME).remove(revokedToken);

            //Remove token from tenant cache.
            removeTokenFromTenantTokenCache(revokedToken, cachedTenantDomain);

        }

        if (renewedToken != null) {

            //Find the actual tenant domain on which the access token was cached. It is stored as a reference in
            //the super tenant cache.
        	// (수정)
            String cachedTenantDomain = 
            		(String) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
            			getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME).get(renewedToken);
//            		Caching.getCachingProvider(RegistryConstants.CACHING_PROVIDER).getCacheManager().
//                    getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME, String.class, String.class).get(renewedToken);

            //Remove the super tenant cache entry.
            // (수정)
//            Caching.getCachingProvider(RegistryConstants.CACHING_PROVIDER).getCacheManager().
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME).remove(renewedToken);

            //Remove token from tenant cache.
            removeTokenFromTenantTokenCache(renewedToken, cachedTenantDomain);

        }
    }

    /**
     * Removes the access token that was cached in the tenant's cache space.
     * @param accessToken - Token to be removed from the cache.
     * @param cachedTenantDomain - Tenant domain from which the token should be removed.
     */
    private void removeTokenFromTenantTokenCache(String accessToken, String cachedTenantDomain){
        //If the token was cached in the tenant cache
        if(cachedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(cachedTenantDomain)){

            if(log.isDebugEnabled()){
                log.debug("Going to remove cache entry " + accessToken + " from " + cachedTenantDomain + " domain");
            }
            try{
                PrivilegedBarleyContext.startTenantFlow();
                PrivilegedBarleyContext.getThreadLocalCarbonContext().
                        setTenantDomain(cachedTenantDomain, true);

                //Remove the tenant cache entry.
                // (수정) 캐쉬 변경 
//                Caching.getCachingProvider(RegistryConstants.CACHING_PROVIDER).getCacheManager().
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                        getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME).remove(accessToken);

                if(log.isDebugEnabled()){
                    log.debug("Removed cache entry " + accessToken + " from " + cachedTenantDomain + " domain");
                }
            } finally {
            	PrivilegedBarleyContext.endTenantFlow();
            }
        }
    }

    public boolean handleRequest(MessageContext messageContext) {
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        clearCacheForAccessToken(messageContext);
        return mediate(messageContext, DIRECTION_OUT);
    }
}