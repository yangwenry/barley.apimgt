/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package barley.apimgt.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.APIProvider;
import barley.apimgt.api.FaultGatewaysException;
import barley.apimgt.api.dto.UserApplicationAPIUsage;
import barley.apimgt.api.model.API;
import barley.apimgt.api.model.APIIdentifier;
import barley.apimgt.api.model.APIStatus;
import barley.apimgt.api.model.BlockConditionsDTO;
import barley.apimgt.api.model.CORSConfiguration;
import barley.apimgt.api.model.Documentation;
import barley.apimgt.api.model.Documentation.DocumentSourceType;
import barley.apimgt.api.model.Documentation.DocumentVisibility;
import barley.apimgt.api.model.DocumentationType;
import barley.apimgt.api.model.DuplicateAPIException;
import barley.apimgt.api.model.Provider;
import barley.apimgt.api.model.Scope;
import barley.apimgt.api.model.SubscribedAPI;
import barley.apimgt.api.model.Subscriber;
import barley.apimgt.api.model.Tier;
import barley.apimgt.api.model.URITemplate;
import barley.apimgt.api.model.policy.BandwidthLimit;
import barley.apimgt.api.model.policy.PolicyConstants;
import barley.apimgt.api.model.policy.QuotaPolicy;
import barley.apimgt.api.model.policy.SubscriptionPolicy;
import barley.apimgt.impl.APIConstants.SubscriptionStatus;
import barley.apimgt.impl.internal.APIManagerComponent;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.APIUtil;
import barley.core.BarleyConstants;
import barley.core.MultitenantConstants;
import barley.core.context.PrivilegedBarleyContext;
import barley.core.utils.BarleyUtils;
import barley.governance.api.util.GovernanceUtils;
import barley.registry.common.AttributeSearchService;
import barley.registry.core.exceptions.RegistryException;
import barley.registry.core.jdbc.EmbeddedRegistryService;
import barley.registry.core.service.TenantRegistryLoader;
import barley.registry.indexing.IndexingManager;
import barley.registry.indexing.Utils;
import barley.registry.indexing.internal.IndexingServiceComponent;
import barley.registry.indexing.service.TenantIndexingLoader;
import barley.registry.indexing.service.TermsSearchService;

public class APIProviderImplTest extends BaseTestCase {

    private static final Log log = LogFactory.getLog(APIProviderImplTest.class);
    
    protected static EmbeddedRegistryService embeddedRegistryService = null;
    
    APIProvider provider = null;
    
    int tenantId = 1;
    String username = "admin@codefarm.co.kr";
    String tenantDomain = "codefarm.co.kr";

    
    public void setUp() throws Exception {
    	super.setUp();
    	
    	// ???????????? 
//      String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantId(tenantId); 
//      int tenantId = PrivilegedBarleyContext.getThreadLocalCarbonContext().getTenantId();
  	
    	//super.setUpCache();
    	setUpRegistry();
    	
    	// activate() ??? ???????????? ???????????? 
    	initializeDatabase();
        /*
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
    	*/
    	
        //apiMgtDAO = ApiMgtDAO.getInstance();
        //IdentityTenantUtil.setRealmService(new TestRealmService());
        
        // (?????????)
        APIManagerComponent.setTenantRegistryLoader(new TenantRegistryLoader() {			
			@Override
			public void loadTenantRegistry(int tenantId) {
				
			}
		});
        ServiceReferenceHolder.getInstance().setIndexLoaderService(new TenantIndexingLoader() {
			@Override
			public void loadTenantIndex(int tenantId) {
				
			}
		});
        
        ServiceReferenceHolder.getInstance().setRegistryService(embeddedRegistryService);
        ServiceReferenceHolder.getInstance().setRealmService(ctx.getRealmService());
        
    	APIManagerComponent component = new APIManagerComponent();
//        component.addRxtConfigs();
//        component.addTierPolicies();
    	component.activate();
    	    	
        // activate() ??? ???????????? ????????????
        /*
        APIUtil.loadTenantExternalStoreConfig(MultitenantConstants.SUPER_TENANT_ID);
        APIUtil.createSelfSignUpRoles(MultitenantConstants.SUPER_TENANT_ID);
        
        APIUtil.loadTenantConf(tenantId);
        APIUtil.loadTenantSelfSignUpConfigurations(tenantId);
        // subscription??? ?????????????????? ??????????????? ???????????? ??????. ????????? /workflowextensions/workflow-extensions.xml??? ????????? system registry??? ?????? 
        APIUtil.loadTenantWorkFlowExtensions(tenantId);
        
        SQLConstantManagerFactory.initializeSQLConstantManager();
        
    	String identityConfigPath = BarleyUtils.getCarbonConfigDirPath() + File.separator + "identity" + File.separator + "identity.xml";
        IdentityConfigParser.getInstance(identityConfigPath);
        */
    	
        // indexing ??????
        AttributeSearchService attributeSearchService = new IndexingServiceComponent.AttributeSearchServiceImpl();
        GovernanceUtils.setAttributeSearchService(attributeSearchService);
        TermsSearchService termsSearchService = new IndexingServiceComponent.TermsSearchServiceImpl();
        GovernanceUtils.setTermsSearchService(termsSearchService);        
        barley.registry.indexing.Utils.setRegistryService(embeddedRegistryService);
        try {
            if (Utils.isIndexingConfigAvailable()) {
            	// (??????)
                //IndexingManager.getInstance().startIndexing();
            } else {
                log.debug("<indexingConfiguration/> not available in registry.xml to start the resource indexing task");
            }
        } catch (RegistryException e) {
            log.error("Failed to start resource indexing task");
        }
        
        // lifecyle ????????? 
        barley.governance.lcm.util.CommonUtil.addDefaultLifecyclesIfNotAvailable(embeddedRegistryService.getConfigSystemRegistry(),
        		embeddedRegistryService.getRegistry(BarleyConstants.REGISTRY_SYSTEM_USERNAME));        
        barley.governance.lcm.util.CommonUtil.addDefaultLifecyclesIfNotAvailable(embeddedRegistryService.getConfigSystemRegistry(tenantId),
        		embeddedRegistryService.getConfigSystemRegistry(tenantId));
        
        barley.governance.registry.extensions.utils.CommonUtil.loadDependencyGraphMaxDepthConfig();

        
        provider = APIManagerFactory.getInstance().getAPIProvider("admin@codefarm.co.kr");
//        provider = APIManagerFactory.getInstance().getAPIProvider("wso2.system.user@carbon.super");
    }
    
    private void setUpRegistry() {
        if (embeddedRegistryService != null) {
            return;
        }

        try {
            embeddedRegistryService = ctx.getEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp = new RealmUnawareRegistryCoreServiceComponent();
            comp.setRealmService(ctx.getRealmService());
            // ?????? ???????????? ???????????????. 
            comp.registerBuiltInHandlers(embeddedRegistryService);
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    private void initializeDatabase() {

    	String configFilePath = BarleyUtils.getCarbonConfigDirPath() + "api-manager.xml";
        InputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(configFilePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            String dataSource = builder.getDocumentElement().getFirstChildWithName(new QName("DataSourceName")).
                    getText();
            OMElement databaseElement = builder.getDocumentElement().getFirstChildWithName(new QName("Database"));
            String databaseURL = databaseElement.getFirstChildWithName(new QName("URL")).getText();
            String databaseUser = databaseElement.getFirstChildWithName(new QName("Username")).getText();
            String databasePass = databaseElement.getFirstChildWithName(new QName("Password")).getText();
            String databaseDriver = databaseElement.getFirstChildWithName(new QName("Driver")).getText();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(databaseDriver);
            basicDataSource.setUrl(databaseURL);
            basicDataSource.setUsername(databaseUser);
            basicDataSource.setPassword(databasePass);

            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES,
                    "org.apache.naming");
            try {
                InitialContext.doLookup("java:/comp/env/jdbc/WSO2AM_DB");
            } catch (NamingException e) {
                InitialContext ic = new InitialContext();
                ic.createSubcontext("java:");
                ic.createSubcontext("java:/comp");
                ic.createSubcontext("java:/comp/env");
                ic.createSubcontext("java:/comp/env/jdbc");

                ic.bind("java:/comp/env/jdbc/WSO2AM_DB", basicDataSource);
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    
    
    // REG_content ???????????? regt_content_data??? ?????? ????????? ??????. 
    public void testAddApi() throws APIManagementException, FaultGatewaysException, DuplicateAPIException {
    	APIIdentifier apiId = new APIIdentifier("yangwenry@codefarm.co.kr", "ncsdept-sample", "v1");
        API api = new API(apiId);
        api.setTitle("?????? ???????????? ?????? api");
        api.setContext("/earth");
        api.setContextTemplate("/earth/{version}");
        api.setStatus(APIStatus.CREATED);
        api.setApiOwner("yangwenry@codefarm.co.kr");
        api.setVisibleRoles("admin");
        api.setVisibility("public");        
        api.setImplementation(APIConstants.IMPLEMENTATION_TYPE_ENDPOINT);
        api.setAsDefaultVersion(true);        
        
        Set<Tier> availableTiers = new HashSet();
        availableTiers.add(new Tier(APIConstants.DEFAULT_SUB_POLICY_SILVER));
        availableTiers.add(new Tier(APIConstants.DEFAULT_SUB_POLICY_GOLD));
        availableTiers.add(new Tier(APIConstants.DEFAULT_SUB_POLICY_UNLIMITED));
        api.addAvailableTiers(availableTiers);
        
        Set<Scope> scopes = new HashSet();
        Scope defaultScope = new Scope();
        defaultScope.setKey("default");
        defaultScope.setName("default");
        scopes.add(defaultScope);
        api.setScopes(scopes);
        
        Set<URITemplate> uriTemplates = new HashSet();
    	URITemplate template = new URITemplate();
    	template.setUriTemplate("/dept");
    	template.setHttpVerbs("GET");
    	template.setHTTPVerb("GET");
    	// AUTH_NO_AUTHENTICATION, AUTH_APPLICATION_LEVEL_TOKEN
    	template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
    	//template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
    	// throttling api level ????????? ?????? ?????? ????????? ?????????. api.setApiLevelPolicy
    	template.setThrottlingTier(APIConstants.DEFAULT_SUB_POLICY_GOLD);    	
    	
    	uriTemplates.add(template);
    	api.setUriTemplates(uriTemplates);
    	
    	CORSConfiguration corsConfiguration = new CORSConfiguration(false, null, false, null, null);
        api.setCorsConfiguration(corsConfiguration);
        
        Set<String> environments = new HashSet<String>();
        environments.add("Production and Sandbox");
        api.setEnvironments(environments);
        
        api.setUrl("http://ncsapi.dev.codefarm.co.kr/api/");
        String endpointConfig = "{\"production_endpoints\":{\"url\":\"http://ncsapi.dev.codefarm.co.kr/api/\",\"config\":null}," + 
        		"\"sandbox_endpoints\":{\"url\":\"http://ncsapi.dev.codefarm.co.kr/api/\",\"config\":null}," +
        		"\"endpoint_type\":\"http\"}";
        api.setEndpointConfig(endpointConfig);
        
        // ?????? 
        provider.addAPI(api);
        
        // endpoint??? ???????????? ?????????..
//        String newVersion = "v2";
////        provider.createNewAPIVersion(api, newVersion);
//        APIIdentifier apiNewId = new APIIdentifier("yangwenry@codefarm.co.kr", "ncsdept-sample", newVersion);
//        provider.deleteAPI(apiNewId);
        
        // ?????????  changeAPIStatus()?????? changeLifeCycleStatus()??? ???????????? ??????. (???????????? ??????.) 
//        String userId = "yangwenry";
//        provider.changeAPIStatus(api, APIStatus.PUBLISHED, userId, true);
        
//      String action = "Publish";
//    	String action = "Demote to Created";
//    	provider.changeLifeCycleStatus(apiId, action);
    	
    	// ?????? 
//    	provider.deleteAPI(apiId);
    }
    
    public void testAddApiAndPublish() throws APIManagementException, FaultGatewaysException {
    	APIIdentifier apiId = new APIIdentifier("admin@codefarm.co.kr", "ncsdept", "1.0.0");
        API api = new API(apiId);
        api.setTitle("ncsdept sample");
        api.setContext("/ncsdept/1.0.0");
        api.setContextTemplate("/ncsdept/{version}");
        api.setStatus(APIStatus.CREATED);
        api.setApiOwner("yangwenry@codefarm.com");
        api.setVisibleRoles("admin");
        api.setVisibility("public");
        api.setImplementation(APIConstants.IMPLEMENTATION_TYPE_ENDPOINT);        
        //prototyped
//      api.setImplementation(APIConstants.IMPLEMENTATION_TYPE_INLINE);
        // endpoint??? registry??? ?????? 
//        api.setUrl("http://ncsapi.dev.codefarm.co.kr/api/");
        api.setAsDefaultVersion(true);
        
        Set<Scope> scopes = new HashSet();
        Scope defaultScope = new Scope();
        defaultScope.setKey("default");
        defaultScope.setName("default");
        scopes.add(defaultScope);
        api.setScopes(scopes);
        
        Set<URITemplate> uriTemplates = new HashSet();
    	URITemplate template = new URITemplate();
    	template.setUriTemplate("/dept");
    	template.setHttpVerbs("GET");
    	template.setHTTPVerb("GET");
    	// AUTH_NO_AUTHENTICATION, AUTH_APPLICATION_LEVEL_TOKEN
    	template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
    	//template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
    	// throttling api level ????????? ?????? ?????? ????????? ?????????. api.setApiLevelPolicy
    	template.setThrottlingTier(APIConstants.DEFAULT_SUB_POLICY_GOLD);    	
    	
    	uriTemplates.add(template);
    	api.setUriTemplates(uriTemplates);    	
    	    	
    	CORSConfiguration corsConfiguration = new CORSConfiguration(false, null, false, null, null);
        api.setCorsConfiguration(corsConfiguration);
        
        // amConfig.xml ?????? 
        Set<String> environments = new HashSet<String>();
        environments.add("Production and Sandbox");
        api.setEnvironments(environments);
        
        String endpointConfig = "{\"production_endpoints\":{\"url\":\"http://ncsapi.dev.codefarm.co.kr/api/\",\"config\":null}," + 
        		"\"sandbox_endpoints\":{\"url\":\"http://ncsapi.dev.codefarm.co.kr/api/\",\"config\":null}," +
        		"\"endpoint_type\":\"http\"}";        				
        api.setEndpointConfig(endpointConfig);
        
    	provider.addAPI(api);
    	
    	String userId = "yangwenry";
    	provider.changeAPIStatus(api, APIStatus.PUBLISHED, userId, true);
    	
    }
    
    
    // REG_content ???????????? regt_content_data??? ?????? ????????? ??????. (????????? ????????? 'application/vnd.wso2.registry-ext-type+xml')
    public void testDocument() throws APIManagementException, FileNotFoundException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	String docName = "howto"; 
    	Documentation doc = new Documentation(DocumentationType.HOWTO, docName);    	
    	doc.setSummary("????????? ??????");
    	doc.setVisibility(DocumentVisibility.API_LEVEL);
    	doc.setSourceType(DocumentSourceType.INLINE);    	
//    	provider.addDocumentation(apiId, doc);
    	
    	assertTrue(provider.isDocumentationExist(apiId, "howto"));
    	
    	doc = provider.getDocumentation(apiId, DocumentationType.HOWTO, docName);
    	assertNotNull(doc);
    	
    	doc.setSummary("????????? ???????????? ???????????? ???????????????.");
    	provider.updateDocumentation(apiId, doc);
    	
    	String text = "???????????????.";
    	API api = getApi();
    	provider.addDocumentationContent(apiId, docName, text);
    	
    	// DocumentSourceType.FILE??? ???????????? ????????????. 
//    	String fileName = "aa.txt";
//    	FileInputStream content = new FileInputStream("D:\\temp\\aa.txt");
//    	String contentType = "txt";
//    	provider.addFileToDocumentation(apiId, doc, fileName, content, contentType);
    	
    	// 3.0.0 ???????????? ????????? ??????????????? 3????????? ????????? registry??? ??????????????? ??????. 
    	APIIdentifier toApiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "3.0.0");
//    	provider.copyAllDocumentation(apiId, "3.0.0");
    	doc = provider.getDocumentation(toApiId, DocumentationType.HOWTO, docName);
    	assertNotNull(doc);
    	
//    	provider.removeDocumentation(apiId, doc.getId());
//    	provider.removeDocumentation(apiId, DocumentationType.HOWTO.toString(), docName);
    	
    }
    
    // ???????????? 
    public void testChangeStatus() throws Exception {
//        String userId = "wso2.system.user";
//        API api = getApi();
//        api.setBusinessOwner("?????????");
    	
        // api ??????
//    	provider.changeAPIStatus(api, APIStatus.PUBLISHED, userId, true);
    	
    	APIIdentifier apiId = new APIIdentifier("admin@codefarm.co.kr", "ncsdept", "1.0.0");
//    	provider.updateAPIStatus(apiId, "PUBLISHED", false, false, true);    	
    	provider.changeLifeCycleStatus(apiId, "Publish");
    }
    
    public void testUpdateApi() throws Exception {
    	API api = getPublishedApi();
    	
//    	Set<URITemplate> uriTemplates = new HashSet();
//    	URITemplate template = new URITemplate();
//    	template.setUriTemplate("/dept");
//    	template.setHttpVerbs("GET");
//    	template.setHTTPVerb("GET");
//    	// AUTH_NO_AUTHENTICATION, AUTH_APPLICATION_LEVEL_TOKEN
//    	template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
//    	//template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
//    	// throttling api level ????????? ?????? ?????? ????????? ?????????. api.setApiLevelPolicy
//    	template.setThrottlingTier(APIConstants.DEFAULT_SUB_POLICY_GOLD);    	
//    	
//    	uriTemplates.add(template);
//    	api.setUriTemplates(uriTemplates);
    	
    	api.setTitle("?????? ?????? API");
    	
    	provider.updateAPI(api);
    	
    	// ???????????? ??? updateAPI ?????? 
//    	provider.manageAPI(api);
    }
    
    public void testCheckApi() throws Exception {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	assertTrue(provider.checkIfAPIExists(apiId));
    	assertTrue(provider.isAPIAvailable(apiId));
    	Set<String> versions = provider.getAPIVersions("wso2.system.user@carbon.super", "WSO2Earth");
    	assertTrue(versions.contains("2.0.0"));    	
    }
    
    public void testDelApi() throws Exception {
    	//APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "ncsdept", "1.0.0");
    	provider.deleteAPI(apiId);
    }
    
    public void testSearchApi() throws APIManagementException {
    	// ???????????? 
    	String searchTerm = "ncsdept";
    	String searchType = "";
    	Map<Documentation, API> termResult = provider.searchAPIsByDoc(searchTerm, searchType);
    	for( Documentation key : termResult.keySet() ){
            System.out.println( String.format("term ??? : %s, ??? : %s", key, termResult.get(key)) );
        }
    	assertNotNull(termResult);
    	
    	String tenantDomain = "codefarm.co.kr";
    	Map<String, Object> apiResult = provider.getAllPaginatedAPIs(tenantDomain, 0, 10);
    	
        for( String key : apiResult.keySet() ){
            System.out.println( String.format("api ??? : %s, ??? : %s", key, apiResult.get(key)) );
        }    	
    	assertNotNull(apiResult);
    }
    
    // ???????????? 
    public void testGetProvider() throws APIManagementException {
    	Provider apiProvider = provider.getProvider("wso2.system.user@carbon.super");
    	assertEquals("wso2.system.user", apiProvider.getName());
    	
//    	Set<Provider> providers = provider.getAllProviders();
//    	assertEquals(1, providers.size());
    	
    }
    
    // reg_association ????????? ?????? 
    public void testGetApiByProvider() throws APIManagementException {
    	List<API> apis = provider.getAPIsByProvider("wso2.system.user@carbon.super");
    	assertEquals(1, apis.size());    	
    }
    
    public void testSubscriber() throws APIManagementException {
    	String providerId = "wso2.system.user@carbon.super";
    	Set<Subscriber> subs = provider.getSubscribersOfProvider(providerId);
    	assertEquals(1, subs.size());
    }
    
    public void testUsage() throws APIManagementException {
    	// null ?????????. ????????? ???????????????.
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
//    	Usage usage = provider.getUsageByAPI(apiId);
//    	assertNotNull(usage);
    	
    	String providerId = "wso2.system.user@carbon.super";
    	UserApplicationAPIUsage[] usages = provider.getAllAPIUsageByProvider(providerId);
    	assertEquals(1, usages.length);
    	
    	List<SubscribedAPI> apis = provider.getAPIUsageByAPIId(apiId);
    	assertEquals(1, apis.size());
    }
    
    // AM_POLICY_THROTTLING ????????? ?????? (DAO????????? ??????)
    public void testPolicies() throws APIManagementException {
//    	Policy[] policies = null;
//    	
//    	APIPolicy policy = new APIPolicy("mypolicy");
//    	policy.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
//    	policy.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
//    	policy.setDisplayName("my policy");
//    	QuotaPolicy quota = new QuotaPolicy();
//    	Limit limit = new BandwidthLimit();
//    	limit.setTimeUnit("min");
//    	limit.setUnitTime(5);
//    	quota.setLimit(limit);
//    	quota.setType(PolicyConstants.BANDWIDTH_TYPE);
//    	policy.setDefaultQuotaPolicy(quota);
//    	
//    	provider.addPolicy(policy);
//    	provider.updatePolicy(policy);
//    	
//    	policies = provider.getPolicies(username, PolicyConstants.POLICY_LEVEL_API);
//    	assertEquals(4, policies.length);
//    	
//    	policy = provider.getAPIPolicy(username, "mypolicy");
//    	assertNotNull(policy);
//    	
//    	// api_throttling policy - ????????? application, subscription, global, api_throttling
//    	String uuid = "ee581b6e-b5a0-4234-b0f8-78860440dd25";
//    	policy = provider.getAPIPolicyByUUID(uuid);
//    	assertNotNull(policy);
//    	
//    	provider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_API, "mypolicy");
    	
    	
    	// application policy
//    	ApplicationPolicy appPolicy = new ApplicationPolicy("myAppPolicy");
//    	appPolicy.setTenantDomain("codefarm.com");
//    	appPolicy.setTenantId(tenantId);
//    	appPolicy.setDisplayName("my policy");
//    	QuotaPolicy quota = new QuotaPolicy();    	
//    	BandwidthLimit limit = new BandwidthLimit();
//    	limit.setTimeUnit("min");
//    	limit.setUnitTime(5);
//    	quota.setLimit(limit);
//    	quota.setType(PolicyConstants.BANDWIDTH_TYPE);
//    	appPolicy.setDefaultQuotaPolicy(quota);    	
//    	provider.addPolicy(appPolicy);
//    	
//    	String policyName = "myAppPolicy";
//    	appPolicy = provider.getApplicationPolicy(username, policyName);
//    	assertNotNull(appPolicy);
    	
//    	uuid = "cf38df14-6457-4133-9405-ad2f6d87c843";
//    	appPolicy = provider.getApplicationPolicyByUUID(uuid);
//    	assertNotNull(appPolicy);
//    	
//    	provider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_APP, "myAppPolicy");
    	
    	
    	// subscription policy - billing plan??? ?????????
    	SubscriptionPolicy subPolicy = new SubscriptionPolicy("yellow");
    	subPolicy.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
//    	subPolicy.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
    	subPolicy.setTenantId(1);
    	subPolicy.setDisplayName("yellow");  
    	subPolicy.setBillingPlan("usage");
    	QuotaPolicy quota = new QuotaPolicy();    	
    	BandwidthLimit limit = new BandwidthLimit();
    	limit.setTimeUnit("min");
    	limit.setUnitTime(5);
    	quota.setLimit(limit);
    	quota.setType(PolicyConstants.BANDWIDTH_TYPE);
    	subPolicy.setDefaultQuotaPolicy(quota);
    	
    	provider.addPolicy(subPolicy);
    	
//    	policyName = "mySubPolicy";
//    	subPolicy = provider.getSubscriptionPolicy(username, policyName);
//    	assertNotNull(subPolicy);
//    	
//    	uuid = "8d9b58b0-4b6b-41dd-b532-1d370f60a641";
//    	subPolicy = provider.getSubscriptionPolicyByUUID(uuid);
//    	assertNotNull(subPolicy);
//    	
//    	String[] policyNames = provider.getPolicyNames(username, PolicyConstants.POLICY_LEVEL_SUB);
//    	assertEquals(1, policyNames.length);
//    	
//    	assertTrue(provider.hasAttachments(username, policyName, PolicyConstants.POLICY_LEVEL_SUB));
//    	
//    	provider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_SUB, "mySubPolicy");
    	
    	
    	// global policy - ThrottlePolicyDeploymentManager ??????     	
//    	GlobalPolicy globalPolicy = new GlobalPolicy("myGlobalPolicy");
//    	globalPolicy.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
//    	globalPolicy.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
//    	globalPolicy.setDisplayName("my policy");  
//    	QuotaPolicy quota = new QuotaPolicy();    	
//    	BandwidthLimit limit = new BandwidthLimit();
//    	limit.setTimeUnit("min");
//    	limit.setUnitTime(5);
//    	quota.setLimit(limit);
//    	quota.setType(PolicyConstants.BANDWIDTH_TYPE);
//    	globalPolicy.setDefaultQuotaPolicy(quota);
//    	provider.addPolicy(globalPolicy);
//    	
//    	String policyName2 = "myGlobalPolicy";
//    	GlobalPolicy globalPolicy2 = provider.getGlobalPolicy(policyName2);
//    	assertNotNull(globalPolicy2);
//    	
//    	assertTrue(provider.isGlobalPolicyKeyTemplateExists(globalPolicy2));
//    	
//    	uuid = "8d9b58b0-4b6b-41dd-b532-1d370f60a641";
//    	globalPolicy = provider.getGlobalPolicyByUUID(uuid);
//    	assertNotNull(globalPolicy);
//    	
//    	provider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_GLOBAL, "myGlobalPolicy");
    }
    
    public void testAddSubscriptionPolicy() throws APIManagementException {
    	SubscriptionPolicy subPolicy = new SubscriptionPolicy("Silver");
    	subPolicy.setTenantDomain(tenantDomain);
    	subPolicy.setTenantId(tenantId);
    	subPolicy.setDisplayName("Silver");  
    	subPolicy.setBillingPlan("usage");
    	subPolicy.setDescription("Allows 2000 requests per minute");
    	QuotaPolicy quota = new QuotaPolicy();    	
    	BandwidthLimit limit = new BandwidthLimit();
    	limit.setTimeUnit("min");
    	limit.setUnitTime(5);
    	quota.setLimit(limit);
    	quota.setType(PolicyConstants.BANDWIDTH_TYPE);
    	subPolicy.setDefaultQuotaPolicy(quota);    	
    	provider.addPolicy(subPolicy);
    	
    	SubscriptionPolicy subPolicy2 = new SubscriptionPolicy("Bronze");
    	subPolicy2.setTenantDomain(tenantDomain);
    	subPolicy2.setTenantId(tenantId);
    	subPolicy2.setDisplayName("Bronze");  
    	subPolicy2.setBillingPlan("usage");
    	subPolicy2.setDescription("Allows 1000 requests per minute");
    	QuotaPolicy quota2 = new QuotaPolicy();    	
    	BandwidthLimit limit2 = new BandwidthLimit();
    	limit2.setTimeUnit("min");
    	limit2.setUnitTime(5);
    	quota2.setLimit(limit2);
    	quota2.setType(PolicyConstants.BANDWIDTH_TYPE);
    	subPolicy2.setDefaultQuotaPolicy(quota2);
    	provider.addPolicy(subPolicy2);
    }
    
    // REG_RESOURCE ???????????? reg_content_id ???????????? ??????. 
    // SELECT * FROM REG_content where reg_content_id = 1207; ???????????? xml ???????????? ??????????????? ????????? ??????.
    // ?????????????????? api ?????? ??? policy-key ????????? ?????????. ????????? ThrottleHandler??? ??????????????? ????????? ???????????? ?????????.   
    public void testTiers() throws APIManagementException {
    	Set<Tier> tiers = provider.getAllTiers(tenantDomain);
    	assertEquals(5, tiers.size());
    	
    	String tierName = "Lemon";
    	
    	Tier addTier = new Tier(tierName);
    	addTier.setRequestCount(10000);
    	addTier.setRequestsPerMin(1000);
    	addTier.setTierPlan("FREE");
    	addTier.setDescription("mytier");
    	addTier.setStopOnQuotaReached(true);
    	addTier.setUnitTime(10000);
    	
//    	provider.addTier(addTier);
//    	provider.updateTier(addTier);
    	
    	// ??????: https://docs.wso2.com/display/AM210/apidocs/publisher/#!/operations#ThrottlingTierIndividual#tiersUpdatePermissionPost
//    	String permissionType = APIConstants.TIER_PERMISSION_ALLOW;
//    	String roles = "admin";
//    	provider.updateTierPermissions(tierName, permissionType, roles);
    	
//    	tiers = provider.getAllTiers(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
//    	assertEquals(5, tiers.size());
    	
    	// ????????????. governance.search ??? ????????? ??????. query ????????? ???????????? ?????? ??????????????? ???????????? ????????????. 
    	provider.removeTier(addTier);
    	
//    	Set<TierPermissionDTO> permissions = provider.getTierPermissions();
//    	assertEquals(1, permissions.size());
//    	
//    	provider.updateThrottleTierPermissions(tierName, permissionType, roles);
//    	permissions = provider.getThrottleTierPermissions();
//    	assertEquals(1, permissions.size());
    }
    
    // AM_SUBSCRIBER ????????? 
    // AM_SUBSCRIPTION
    // AM_APPLICATION
    // AM_API
    public void testSubscriptions() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	Set<Subscriber> subscribers = provider.getSubscribersOfAPI(apiId);
    	
    	assertEquals(1, provider.getAPISubscriptionCountByAPI(apiId));
    	assertEquals(1, subscribers.size());
    	
    	String uuid = "931d50e6-6e84-447e-b595-74e8a70b524d";
    	SubscribedAPI subscribedAPI = provider.getSubscriptionByUUID(uuid);
    	
    	// SUB_STATUS ?????? ??????
    	subscribedAPI.setSubStatus(SubscriptionStatus.ON_HOLD);
    	provider.updateSubscription(subscribedAPI);
    	
    	// appId??? ?????????????????? SUB_STATUS ?????? ?????? 
//    	int appId = 5;
//    	provider.updateSubscription(apiId, SubscriptionStatus.ON_HOLD, appId);    	
    }

    public void testLifecycle() throws APIManagementException, FaultGatewaysException {
//    	provider = APIManagerFactory.getInstance().getAPIProvider("wso2.system.user@carbon.super");
//    	String tenantDomain = "carbon.super";
//    	String config = provider.getLifecycleConfiguration(tenantDomain);
//    	assertNotNull(config);
    	
    	provider = APIManagerFactory.getInstance().getAPIProvider("admin@codefarm.co.kr");
//    	String getLifecycleConfiguration = provider.getLifecycleConfiguration("codefarm.co.kr");
//    	assertNotNull(getLifecycleConfiguration);
    	
    	APIIdentifier apiId = new APIIdentifier("admin@codefarm.co.kr", "myapi", "v1");
//    	Map<String, Object> result = provider.getAPILifeCycleData(apiId);
//    	for( String key : result.keySet() ){    		
//            System.out.println( String.format("api ??? : %s, ??? : %s", key, result.get(key)) );
//            if("nextStates".equals(key)) {
//            	String[] stages = (String[]) result.get(key);
//            	for(String stage : stages) {
//            		System.out.println(stage);
//            	}
//            }
//        }
//    	assertNotNull(result);
    	
//    	APIIdentifier apiId = new APIIdentifier("yangwenry@codefarm.com", "ncsdept", "1.0.0");
//    	List<LifeCycleEvent> events = provider.getLifeCycleEvents(apiId);
    	// created, published 2?????? ??????.
//    	assertEquals(2, events.size());
    	
    	// registry.xml??? ?????? lifecycle ?????? ??????.
    	// Preprequest action must be completed before Published
//    	String action = "Publish";
    	String action = "Demote to Created";
    	provider.changeLifeCycleStatus(apiId, action);
    	
//    	Map<String, Object> result = provider.getAPILifeCycleData(apiId);
//    	assertNotNull(result);
    	
    	// ????????????.
//    	String checkItemName = "Require re-subscription when publish the API";    	
//    	provider.checkAndChangeAPILCCheckListItem(apiId, checkItemName, true);
    	
//    	String status = provider.getAPILifeCycleStatus(apiId);
//    	assertNotNull(status);
    	
    	// registry??? ????????? ?????? - changeLifeCycleStatus()?????? ???????????? ??????
//    	provider.updateAPIforStateChange(apiId, APIStatus.PUBLISHED, null);
    	
    	// gateway??? ????????? ?????? - changeLifeCycleStatus()?????? ???????????? ??????
//    	provider.propergateAPIStatusChangeToGateways(apiId, APIStatus.PUBLISHED);
    	
    }
    
    public void testGetAllApi() throws APIManagementException {
//    	List<API> apis = provider.searchAPIs("WSO2", "Name", "wso2.system.user@carbon.super");
//    	assertEquals(1, apis.size());
    	
    	provider = APIManagerFactory.getInstance().getAPIProvider("admin@codefarm.co.kr");
    	List<API> apis = provider.getAllAPIs();
    	assertEquals(1, apis.size());
    }
    
    public void testBlockCondition() throws APIManagementException {
    	//provider.addBlockCondition(APIConstants.BLOCKING_CONDITIONS_IP, "192.168.201.23");
    	
    	int conditionId = 1;
    	BlockConditionsDTO conditions = provider.getBlockCondition(conditionId);
    	assertEquals(1, conditions.getConditionId());
    	
    	String uuid = "efc18992-9bac-4c55-bf5c-527fa168ae17";
    	conditions = provider.getBlockConditionByUUID(uuid);
    	assertEquals(1, conditions.getConditionId());
    	
    	provider.updateBlockCondition(conditionId, "true");
    	
    	List<BlockConditionsDTO> conditionsList = provider.getBlockConditions();
    	assertEquals(1, conditionsList.size());
    	
    	provider.deleteBlockCondition(conditionId);
    	conditionsList = provider.getBlockConditions();
    	assertEquals(0, conditionsList.size());
    }    
    
    public void testKey() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "ncsdept", "1.0.0");
    	String[] keys = provider.getConsumerKeys(apiId);
    	assertEquals(1, keys.length);
    	
//    	API api = getApi();
//    	provider.makeAPIKeysForwardCompatible(api);
    }
    
    public void testSwagger() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	String jsonText = "{}";
    	provider.saveSwagger20Definition(apiId, jsonText);
    	
    	String definition = provider.getSwagger20Definition(apiId);
    	assertNotNull(definition);
    }
    
    public void testApiNewVersion() throws APIManagementException, DuplicateAPIException {
    	API api = getApi();
    	api.setAsDefaultVersion(true);
    	provider.createNewAPIVersion(api, "3.0.0");
    }
    
    public void testVersions() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "3.0.0");
    	String version = provider.getDefaultVersion(apiId);
    	assertNotNull(version);
    }
    
    public void testSequences() throws APIManagementException {
    	List<String> seq = provider.getCustomInSequences();
    	assertNotNull(seq);
    }
    
    public void testEtc() throws APIManagementException, RegistryException {
    	//assertFalse(provider.isSynapseGateway());
    	
    	String path = "/repository/components/org.wso2.carbon.governance/lifecycles";
    	String username = "admin@codefarm.co.kr";
    	embeddedRegistryService.getUserRegistry(username).delete(path);
    }
    
        
    
    private API getApi() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	API api = provider.getAPI(apiId);
    	return api;
    }
    
    private API getPublishedApi() throws APIManagementException {
    	String apiName = "Google Directions API";
		String version = "v1";
    	APIIdentifier apiId = new APIIdentifier("admin@codefarm.co.kr", apiName, version);
    	API api = provider.getAPI(apiId);
    	return api;
    } 
    
    public void testAviableTier() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "nexus1", "nexus3");
    	API api = provider.getAPI(apiId);
    	Set<Tier> tiers = api.getAvailableTiers();
    	for(Tier tier : tiers) {
    		System.out.println(tier.getName());
    	}
    	assertNotNull(api);
    }
    
    public void testGetApi() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("yangwenry@codefarm.co.kr", "ncsdept", "v1");
    	API api = provider.getAPI(apiId);
    	assertNotNull(api);
    }
    
}


