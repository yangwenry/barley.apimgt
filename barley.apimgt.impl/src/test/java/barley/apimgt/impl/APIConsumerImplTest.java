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

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
import org.mockito.Mockito;

import barley.apimgt.api.APIConsumer;
import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.model.API;
import barley.apimgt.api.model.APIIdentifier;
import barley.apimgt.api.model.APIRating;
import barley.apimgt.api.model.Application;
import barley.apimgt.api.model.Comment;
import barley.apimgt.api.model.Documentation;
import barley.apimgt.api.model.Scope;
import barley.apimgt.api.model.SubscribedAPI;
import barley.apimgt.api.model.Subscriber;
import barley.apimgt.api.model.SubscriptionResponse;
import barley.apimgt.api.model.Tag;
import barley.apimgt.impl.factory.KeyManagerHolder;
import barley.apimgt.impl.factory.SQLConstantManagerFactory;
import barley.apimgt.impl.internal.APIManagerComponent;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.APIMgtDBUtil;
import barley.apimgt.impl.utils.APIUtil;
import barley.core.MultitenantConstants;
import barley.core.context.PrivilegedBarleyContext;
import barley.governance.api.util.GovernanceUtils;
import barley.identity.core.util.IdentityConfigParser;
import barley.registry.api.Resource;
import barley.registry.common.AttributeSearchService;
import barley.registry.core.exceptions.RegistryException;
import barley.registry.core.jdbc.EmbeddedRegistryService;
import barley.registry.core.service.TenantRegistryLoader;
import barley.registry.core.session.UserRegistry;
import barley.registry.indexing.Utils;
import barley.registry.indexing.internal.IndexingServiceComponent;
import barley.registry.indexing.service.TenantIndexingLoader;
import barley.registry.indexing.service.TermsSearchService;
import barley.user.api.UserStoreException;
import junit.framework.Assert;

public class APIConsumerImplTest extends BaseTestCase {

	private static final Log log = LogFactory.getLog(APIConsumerImplTest.class);
    
    protected static EmbeddedRegistryService embeddedRegistryService = null;
    
    APIConsumer consumer;
        
    public void setUp() throws Exception {
    	super.setUp();
    	
        // 세션처리 
//      String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	String tenantDomain = "codefarm.co.kr";
    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);    	
    	int tenantId = PrivilegedBarleyContext.getThreadLocalCarbonContext().getTenantId();

    	super.setUpCache();
    	setUpRegistry();
    	
    	//(오세창)
    	String baseDir = "D:\\Workspace_STS_SaaSPlatform\\Workspace_STS_APIM\\barley.apimgt\\barley.apimgt.impl";
    	System.setProperty("APIManagerDBConfigurationPath", baseDir + "/src/test/resources/repository/conf/api-manager.xml");
    	System.setProperty("IdentityConfigurationPath", baseDir + "/src/test/resources/repository/conf/identity/identity.xml");
    	
    	String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
        //apiMgtDAO = ApiMgtDAO.getInstance();
        //IdentityTenantUtil.setRealmService(new TestRealmService());
        
        // (오세창)
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
        
    	//RegistryService registryService = new RemoteRegistryService("","","");
    	//ServiceReferenceHolder.getInstance().setRegistryService(registryService);
        
        APIManagerComponent component = new APIManagerComponent();
        component.addRxtConfigs();
        component.addTierPolicies();
    	
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
        
        APIUtil.loadTenantExternalStoreConfig(MultitenantConstants.SUPER_TENANT_ID);
        APIUtil.createSelfSignUpRoles(MultitenantConstants.SUPER_TENANT_ID);
        
        APIUtil.loadTenantConf(tenantId);
        APIUtil.loadTenantSelfSignUpConfigurations(tenantId);
        // subscription을 하기위해서는 워크플로를 로딩해야 한다. 초기에 /workflowextensions/workflow-extensions.xml에 읽어와 system registry에 저장 
        APIUtil.loadTenantWorkFlowExtensions(tenantId);
        
        // Initialize KeyManager.
        KeyManagerHolder.initializeKeyManager(config);
        // Initialize sql constants
        SQLConstantManagerFactory.initializeSQLConstantManager();
        
        // indexing 처리
        AttributeSearchService attributeSearchService = new IndexingServiceComponent.AttributeSearchServiceImpl();
        GovernanceUtils.setAttributeSearchService(attributeSearchService);        
        TermsSearchService termsSearchService = new IndexingServiceComponent.TermsSearchServiceImpl();
        GovernanceUtils.setTermsSearchService(termsSearchService);
        
        barley.registry.indexing.Utils.setRegistryService(embeddedRegistryService);
        try {
            if (Utils.isIndexingConfigAvailable()) {
                //IndexingManager.getInstance().startIndexing();
            } else {
                log.debug("<indexingConfiguration/> not available in registry.xml to start the resource indexing task");
            }
        } catch (RegistryException e) {
            log.error("Failed to start resource indexing task");
        }
        
        consumer = APIManagerFactory.getInstance().getAPIConsumer("user01@codefarm.co.kr");
//        consumer = APIManagerFactory.getInstance().getAPIConsumer("wso2.system.user@carbon.super");
    }
    
    private void setUpRegistry() {
        if (embeddedRegistryService != null) {
            return;
        }

        try {
            embeddedRegistryService = ctx.getEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp = new RealmUnawareRegistryCoreServiceComponent();
            comp.setRealmService(ctx.getRealmService());
            // 각종 핸들러를 추가해준다. 
            comp.registerBuiltInHandlers(embeddedRegistryService);
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    private void initializeDatabase(String configFilePath) {

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
    
    
    public void testReadMonetizationConfigAnnonymously() {
        APIMRegistryService apimRegistryService = Mockito.mock(APIMRegistryService.class);

        String json = "{\n  EnableMonetization : true\n }";

        try {
            when(apimRegistryService.getConfigRegistryResourceContent("", "")).thenReturn(json);
            /* TODO: Need to mock out ApimgtDAO and usage of registry else where in order to test this
            APIConsumer apiConsumer = new UserAwareAPIConsumer("__wso2.am.anon__", apimRegistryService);

            boolean isEnabled = apiConsumer.isMonetizationEnabled("carbon.super");

            assertTrue("Expected true but returned " + isEnabled, isEnabled);

        } catch (APIManagementException e) {
            e.printStackTrace();
        */} catch (UserStoreException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test case is to test the URIs generated for tag thumbnails when Tag wise listing is enabled in store page.
     */
    public void testTagThumbnailURLGeneration() {
        // Check the URL for super tenant
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        String thumbnailPath = "/apimgt/applicationdata/tags/wso2-group/thumbnail.png";
        String finalURL = APIUtil.getRegistryResourcePathForUI(APIConstants.
                                                                       RegistryResourceTypesForUI.TAG_THUMBNAIL, tenantDomain,
                                                               thumbnailPath);
        log.info("##### Generated Tag Thumbnail URL > " + finalURL);
        Assert.assertEquals("/registry/resource/_system/governance" + thumbnailPath, finalURL);

        // Check the URL for other tenants
        tenantDomain = "apimanager3155.com";
        finalURL = APIUtil.getRegistryResourcePathForUI(APIConstants.
                                                                       RegistryResourceTypesForUI.TAG_THUMBNAIL, tenantDomain,
                                                               thumbnailPath);
        log.info("##### Generated Tag Thumbnail URL > " + finalURL);
        Assert.assertEquals("/t/" + tenantDomain + "/registry/resource/_system/governance" + thumbnailPath, finalURL);
    }
    
    public void testGetAllApis() throws Exception {
//    	String tenantDomain = "codefarm.co.kr";
    	String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	    	
//    	APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer("wso2.anonymous.user@codefarm.co.kr");
//    	APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer("admin@codefarm.co.kr");
    	APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer("wso2.system.user@carbon.super");
//    	List<API> apis = consumer.getAllAPIs();
//    	assertNotNull(apis);
//    	assertEquals(1, apis.size());
        
        Set<API> sApis = null;
        sApis = consumer.getAllPublishedAPIs(tenantDomain);
        assertNotNull(sApis);
        
        // 오류발생 
//        sApis = consumer.getRecentlyAddedAPIs(1000, tenantDomain);
//        assertTrue(sApis.size() == 1);
        
        // 오류발생 
//        Map<String,Object> pagedApis = consumer.getAllPaginatedPublishedAPIs(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, 0, 10);
//        assertTrue(pagedApis.size() == 1);
        
        // 오류발생 
//        String tag = "xxx";
//        Set<API> tApis = consumer.getAPIsWithTag(tag, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
//        assertTrue(tApis.size() == 1);
       
        // 오류발생 - getAttributeSearchService가 null. 모든 서칭은 에러.
//        Map<String, Object> result = consumer.getAllPaginatedAPIsByStatus(tenantDomain, 0, 10, "Created", false);        
//        consumer.getAllPaginatedPublishedAPIs(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, 0, 10);
//        assertNotNull(result);
        
//        String searchTerm = "WSO2";
//        String searchType = null;
//        // 비어있음
//        consumer.searchAPI(searchTerm, searchType, tenantDomain);
        
        // 오류발생 
//        String searchQuery = "WSO2";
//        consumer.searchPaginatedAPIs(searchQuery, tenantDomain, 0, 10, false);
        
//        String providerId = "wso2.system.user@carbon.super";
//        sApis = consumer.getPublishedAPIsByProvider(providerId, 1000);
//        assertEquals(1, sApis.size());
//        
//        String apiOwner = "wso2.system.user@carbon.super";
//        String apiBizOwner = null;
//        // 로그인이 동작안했기 때문에 0
//        sApis = consumer.getPublishedAPIsByProvider(providerId, providerId, 1000, apiOwner, apiBizOwner);
//        assertEquals(0, sApis.size());
    }
    
    public void testGetApiByUUID() throws Exception {
    	API api = consumer.getAPIbyUUID("2de76310-a588-42c1-8f83-60823b83bf4e", "carbon.super");
    	assertNotNull(api);
    	log.info(api);
    }
    
    public void testGetAllDocs() throws Exception {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");    	
    	List<Documentation> docs = consumer.getAllDocumentation(apiId);
    	assertTrue(docs.size() == 0);
    }
    
    // am_application
    public void testApplication() throws APIManagementException {
    	String userId = "yangwenry@codefarm.co.kr";
    	int applicationId = 6;
    	String groupId = "1";
    	String applicationName = "DefaultApplication";
    	
    	Subscriber subscriber = new Subscriber(userId); 
    	Application newApplication = new Application("DefaultApplication", subscriber);
    	newApplication.setGroupId(groupId);
    	consumer.addApplication(newApplication, userId);
    	
//    	Application application = consumer.getApplicationById(applicationId);
////    	assertEquals("DefaultApplication", application.getName());
//    	
//    	String status = consumer.getApplicationStatusById(applicationId);
//    	assertEquals("APPROVED", status);
//    	
//    	Application[] apps = consumer.getApplicationsWithPagination(subscriber, groupId, 0, 10, "2", "NAME", "ASC");
//    	assertEquals(1, apps.length);
//    	
    	Application application = consumer.getApplicationsByName(userId, applicationName, groupId);
    	assertEquals("DefaultApplication", application.getName());
//    	
//    	apps = consumer.getApplications(subscriber, groupId);
//    	assertEquals(2, apps.length);
    	
//    	application.setDescription("수정중");
//    	consumer.updateApplication(application);    	
//    	consumer.removeApplication(application);	
    }
    
    // am_subscription 테이블 
    public void testSubscription() throws APIManagementException {
    	int applicationId = 3;
    	int newApplicationId = 6;
    	
//    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "ayj1", "ayj3");
    	APIIdentifier apiId = new APIIdentifier("admin@codefarm.co.kr", "ncsdept", "1.0.0");
//    	String username = "user01@codefarm.co.kr";
    	String username = "admin@codefarm.co.kr";
    	SubscriptionResponse res = consumer.addSubscription(apiId, username, applicationId);    	
//    	assertNotNull(res);
//    	SubscribedAPI subscription = consumer.getSubscriptionByUUID(res.getSubscriptionUUID());
    	
//    	int subscriptionId = 26;
//    	SubscribedAPI subscription = consumer.getSubscriptionById(subscriptionId);
//    	assertEquals("DefaultApplication", subscription.getApplication().getName());
    	
//    	String status = consumer.getSubscriptionStatusById(subscriptionId);
//    	assertEquals(SubscriptionStatus.ON_HOLD, status);
    	
    	assertTrue(consumer.isSubscribed(apiId, username));
    	
//    	consumer.updateSubscriptions(apiId, userId, newApplicationId);
//    	consumer.removeSubscription(subscription);
//    	consumer.removeSubscription(apiId, userId, newApplicationId);
    }
    
    // AM_SUBSCRIBER 테이블에 입력 
    public void testSubscriber() throws APIManagementException {
    	int tenantId = 1;
        String username = "yangwenry@codefarm.co.kr";
        String tenantDomain = "codefarm.co.kr";
    	
    	String groupingId = null;    	
    	String applicationName = "DefaultApplication";
    	
    	consumer.addSubscriber(username, groupingId);
    	
    	Subscriber subscriber = consumer.getSubscriber(username);
    	assertNotNull(subscriber);
    	
    	Set<SubscribedAPI> subscribedApis = consumer.getSubscribedAPIs(subscriber);
    	assertNotNull(subscribedApis);
    	
    	subscribedApis = consumer.getSubscribedAPIs(subscriber, groupingId);
    	assertNotNull(subscribedApis);
    	
    	subscribedApis = consumer.getSubscribedAPIs(subscriber, applicationName, groupingId);
    	assertNotNull(subscribedApis);
    	
    	subscribedApis = consumer.getPaginatedSubscribedAPIs(subscriber, applicationName, 0, 10, groupingId);
    	assertNotNull(subscribedApis);
    	
    	int count = consumer.getSubscriptionCount(subscriber, applicationName, groupingId);
    	assertEquals(1, count);
    	
    	// 소스가 구현되지 않았음. 비어있음.
//    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
//    	consumer.removeSubscriber(apiId, userId);
    	
    	APIIdentifier apiId = new APIIdentifier("admin@codefarm.co.kr", "ncsdept", "2.0.0");
    	subscribedApis = consumer.getSubscribedIdentifiers(subscriber, apiId, groupingId);
    	assertNotNull(subscribedApis);
    	
    }
    
    public void testComment() throws APIManagementException {
    	String userId = "wso2.system.user@carbon.super";
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
//    	String comment = "잘 만들어진 api에요~~~";
//    	consumer.addComment(apiId, comment, userId);
    	
    	Comment[] comments = consumer.getComments(apiId);
    	assertEquals(1, comments.length);
    }
    
    // TermsSearchService가 존재하지 않아 동작하지 않음. governance 초기화 코드에서 indexing에 관련된 인스턴스를 주입시켜준다. 
    // 주입된 코드를 참고해야 될 듯 
    // https://github.com/wso2/carbon-registry/blob/release-4.4.0/components/registry/org.wso2.carbon.registry.indexing/src/main/java/org/wso2/carbon/registry/indexing/internal/IndexingServiceComponent.java
    public void testTags() throws APIManagementException {
    	String tenantDomain = "codefarm.com";
        
    	Set<Tag> tags = consumer.getAllTags(tenantDomain);
    	assertEquals(1, tags.size());
    }
    
    public void testApplicationKey() throws APIManagementException {    
    	String username = "admin@codefarm.co.kr";
        
    	String tokenType = APIConstants.API_KEY_TYPE_PRODUCTION;
    	String callbackUrl = null;
    	String allowedDomains[] = null;
    	String tokenScope = null;
    	String jsonString = "{}";
    	String applicationName = "DefaultApplication";
    	String groupId = "1";
    	String validityTime = "100000000000";
    	
    	// 1. am_application_registration, AM_APPLICATION_KEY_MAPPING 테이블에 승인 데이터를 생성한다.
    	// 2. 게이트웨이를 통해 토큰 키를 생성한다. - 현재 주석처리하여 동작하지 않음. 
    	// 어플리케이션 승인요청. IDN_OAUTH_CONSUMER_APPS 테이블에 사용자의 어플이 존재하면 에러발생함.
    	consumer.requestApprovalForApplicationRegistration(username, applicationName, tokenType, callbackUrl, allowedDomains, 
    			validityTime, tokenScope, groupId, jsonString);
    	
    	// 어플리케이션 승인완료 => AM_APPLICATION_KEY_MAPPING 테이블이 승인상태일 경우에만 동작. 토큰키 생성 후 complete 완료.  
    	// SP_APP 테이블에 데이터 생성.
    	consumer.completeApplicationRegistration(username, applicationName, tokenType, tokenScope, groupId);
    	
    	// 승인정보 삭제 
//    	consumer.cleanUpApplicationRegistration(applicationName, tokenType, groupId, username);
    	
//    	String clientId = "12DKfw2R4LE8LfdMMCW4HSp3u3wa";
//    	String keyType = "PRODUCTION";	
    	// 키 맵핑 수행. 서버측에 데이터가 있을경우 어플리케이션 테이블에 데이터를 저장하여 매핑시킨다. 
//    	consumer.mapExistingOAuthClient(jsonString, username, clientId, applicationName, keyType);    	
    	
//    	OAuthApplicationInfo info = consumer.updateAuthClient(username, applicationName, tokenType, callbackUrl, allowedDomains, validityTime
//    			, tokenScope, groupId, jsonString);
//    	assertNotNull(info);
    	
    	// oauth app 삭제 
//    	String consumerKey = "CykrjoAp_aobixZjVnAETPtXI8Aa";
//    	consumer.deleteOAuthApplication(consumerKey);
    	
//    	Set<APIIdentifier> identifiers = consumer.getAPIByConsumerKey(consumerKey);
//    	assertNotNull(identifiers);
    }
    
    public void testRenewApplicationKey() throws APIManagementException {
    	String oldAccessToken = "7c132edac880b71de13d9bf14ee9d278";
    	String clientId = "O7ico7MMf8O2ajXmQRAkuNiRWhsa";
    	String clientSecret = "JBY4RztGdzn2jzfvjQsult2FRMga";
    	String requestScope[] = {"default"};
    	String validityTime = "100000000000";
    	String jsonInput = "{\"client_id\":\"" + clientId + "\", \"client_secret\":\"" + clientSecret + "\", \"validityPeriod\":\"" + validityTime + "\"}";
    	consumer.renewAccessToken(oldAccessToken, clientId, clientSecret, validityTime, requestScope, jsonInput);
    }
    
    public void testApplicationTokenExists() throws APIManagementException {
    	String accessToken = null;
    	consumer.isApplicationTokenExists(accessToken);
    }
    
    public void testTag() throws APIManagementException {
    	String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	Set<Tag> tags = consumer.getAllTags(tenantDomain);
    	assertEquals(0, tags.size());
    	
    	tags = consumer.getTagsWithAttributes(tenantDomain);
    	assertEquals(0, tags.size());
    }
    
    public void testRateApi() throws APIManagementException {
    	String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME; 
    	String userId = "wso2.system.user@carbon.super";
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	
    	Set<API> sApis = consumer.getTopRatedAPIs(1000);
        assertTrue(sApis.size() == 0);
        
        // 별점매기기. 성공적. am_api_ratings 테이블 조회 
        consumer.rateAPI(apiId, APIRating.RATING_FIVE, userId);
        
        int rating = consumer.getUserRating(apiId, userId);
        assertEquals(APIRating.RATING_FIVE.getRating(), rating);
    }
    
    public void testTier() throws APIManagementException {
    	// tier permission에 allow 값이 들어가 있어야 가져온다. 
    	Set<String> tiers = consumer.getDeniedTiers();
    	assertEquals(1, tiers.size());
    	
    	String tierName = "Lemon";
    	assertTrue(consumer.isTierDeneid(tierName));
    }

    public void testScope() throws APIManagementException {
    	// 토큰스코프 가져오기 
    	String accessToken = "2a2f04b50da753bbec5d611d8d0d66ee";
        String scope = consumer.getScopesByToken(accessToken);
        assertNotNull(scope);
         
        // api 스코프 가져오기  
        String scopeKeys = "default";
        int tenantId = -1234;
        Set<Scope> scopes = consumer.getScopesByScopeKeys(scopeKeys, tenantId);
        assertEquals(1, scopes.size());

        // api 스코프 가져오기
        APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "1.0.0");
        List apiIds = new ArrayList();
        apiIds.add(apiId);
        scopes = consumer.getScopesBySubscribedAPIs(apiIds);
        assertEquals(1, scopes.size());
    }
    
    public void testGroupId() throws APIManagementException {
    	String response = "";
    	String groupId = consumer.getGroupIds(response);
    	assertNotNull(groupId);
    }
    
    public void testWorkflow() {
    	String[] args = null;
    	// 파라미터 속성값을 구분할 수 없음 
    	consumer.resumeWorkflow(args);
    }
    
    public void testMonetization() throws APIManagementException {
    	assertFalse(consumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
    	
    }
    
    public void testApiAuth() throws RegistryException {
    	int tenantID = -1234;
    	UserRegistry registry = embeddedRegistryService.getGovernanceUserRegistry("wso2.anonymous.user", tenantID);
    	String url = "/apimgt/applicationdata/provider/wso2.system.user-AT-carbon.super/xyz/1/swagger.json";
    	Resource data = registry.get(url);
    	assertNotNull(data);
    }
    
    public void testEtc() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "test07", "test07");
    	API api = consumer.getAPI(apiId);
    	assertNotNull(api);
    }
}



