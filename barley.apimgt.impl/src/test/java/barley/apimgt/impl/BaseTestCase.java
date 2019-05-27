/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package barley.apimgt.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.cache.CacheConfiguration;
import javax.cache.Caching;

import org.apache.commons.dbcp.BasicDataSource;

import barley.core.ServerConstants;
import barley.core.internal.BarleyContextDataHolder;
import barley.core.internal.OSGiDataHolder;
import barley.registry.core.RegistryConstants;
import barley.registry.core.config.RegistryContext;
import barley.registry.core.internal.RegistryCoreServiceComponent;
import barley.registry.core.jdbc.realm.InMemoryRealmService;
import barley.user.core.service.RealmService;
import junit.framework.TestCase;

public class BaseTestCase extends TestCase {

    protected RegistryContext ctx = null;
    protected InputStream is;

    public void setUp() throws Exception {
        setupCarbonHome();
        setupContext();
        
        OSGiDataHolder.getInstance().setUserRealmService(ctx.getRealmService());
    }

    protected void setupCarbonHome() {
    	/*
        if (System.getProperty("carbon.home") == null) {
            File file = new File("../distribution/kernel/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../distribution/kernel/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        */
        System.setProperty(ServerConstants.CARBON_HOME, "D:/Workspace_STS_SaaSPlatform/Workspace_STS_APIM/barley.apimgt/barley.apimgt.impl/src/test/resources/");
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, "D:/Workspace_STS_SaaSPlatform/Workspace_STS_APIM/barley.apimgt/barley.apimgt.impl/src/test/resources/repository/conf/");
        System.setProperty("registry.config", "registry.xml");
        // BLOB 값을 쓰고 읽을 때 사용 
        System.setProperty("carbon.registry.character.encoding", "UTF-8");
        
        // The line below is responsible for initializing the cache.
        BarleyContextDataHolder.getCurrentCarbonContextHolder();        
    }
    
    protected void setupContext() {
        try {
        	BasicDataSource dataSource = new BasicDataSource();
        	String connectionUrl = "jdbc:mysql://172.16.2.201:3306/barley_registry";
            dataSource.setUrl(connectionUrl);
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUsername("cdfcloud");
            dataSource.setPassword("cdfcloud");
        	
            RealmService realmService = new InMemoryRealmService(dataSource);
            //is = this.getClass().getClassLoader().getResourceAsStream(System.getProperty("registry.config"));
            String registryPath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + System.getProperty("registry.config");
            is = new FileInputStream(new File(registryPath));
            // registry.xml 정보와 DataSource가 가미된 realmService를 인자로 주어 RegistryContext를 생성한다. 
            ctx = RegistryContext.getBaseInstance(is, realmService);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        ctx.setSetup(true);
//        ctx.selectDBConfig("h2-db");
        ctx.selectDBConfig("mysql-db");
    }

    public class RealmUnawareRegistryCoreServiceComponent extends RegistryCoreServiceComponent {

        public void setRealmService(RealmService realmService) {
            super.setRealmService(realmService);
        }
    }
    
    protected void setUpCache() {
    	/*
    	// registry cache 세팅 
        CacheManager cacheManager = Caching.getCachingProvider(RegistryConstants.CACHING_PROVIDER).getCacheManager();
        MutableConfiguration<RegistryCacheKey, RegistryCacheEntry> configuration =
                new MutableConfiguration<RegistryCacheKey, RegistryCacheEntry>()  
                    .setTypes(RegistryCacheKey.class, RegistryCacheEntry.class)   
                    .setStoreByValue(false)   
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY));
        cacheManager.createCache(RegistryConstants.PATH_CACHE_ID, configuration);
        
        MutableConfiguration<RegistryCacheKey, GhostResource> configuration2 =
                new MutableConfiguration<RegistryCacheKey, GhostResource>()  
                    .setTypes(RegistryCacheKey.class, GhostResource.class)   
                    .setStoreByValue(false)   
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY));
        cacheManager.createCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID, configuration2);
        
        // impl cache 세팅 
        MutableConfiguration<String, String> configuration3 =
                new MutableConfiguration<String, String>()  
                    .setTypes(String.class, String.class)   
                    .setStoreByValue(false)   
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY));
        cacheManager.createCache(RegistryConstants.UUID_CACHE_ID, configuration3);
        
        MutableConfiguration<String, Set> configuration4 =
                new MutableConfiguration<String, Set>()  
                    .setTypes(String.class, Set.class)   
                    .setStoreByValue(false)   
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY));
        cacheManager.createCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME, configuration4);        
        
        MutableConfiguration<String, TenantWorkflowConfigHolder> configuration5 =
                new MutableConfiguration<String, TenantWorkflowConfigHolder>()  
                    .setTypes(String.class, TenantWorkflowConfigHolder.class)   
                    .setStoreByValue(false)   
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY));
        cacheManager.createCache(APIConstants.WORKFLOW_CACHE_NAME, configuration5);
        
        MutableConfiguration<String, HashMap> configuration6 =
                new MutableConfiguration<String, HashMap>()  
                    .setTypes(String.class, HashMap.class)   
                    .setStoreByValue(false)   
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY));
        cacheManager.createCache(APIConstants.TIERS_CACHE, configuration6);
        */
    	
    	long sessionCacheTimeout = 1800L;
        Caching.getCacheManager(RegistryConstants.REGISTRY_CACHE_MANAGER).
			createCacheBuilder(RegistryConstants.PATH_CACHE_ID).
			setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
			setStoreByValue(false).build();
        Caching.getCacheManager(RegistryConstants.REGISTRY_CACHE_MANAGER).
			createCacheBuilder(RegistryConstants.REGISTRY_CACHE_BACKED_ID).
			setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
			setStoreByValue(false).build();
        Caching.getCacheManager(RegistryConstants.REGISTRY_CACHE_MANAGER).
			createCacheBuilder(RegistryConstants.UUID_CACHE_ID).
			setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
			setStoreByValue(false).build();
        
        Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
			createCacheBuilder(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).
			setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
			setStoreByValue(false).build();
	    Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
			createCacheBuilder(APIConstants.WORKFLOW_CACHE_NAME).
			setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
			setStoreByValue(false).build();
        Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
			createCacheBuilder(APIConstants.TIERS_CACHE).
			setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
			setStoreByValue(false).build();
    }
}
