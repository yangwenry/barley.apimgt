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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.HashMap;
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

import barley.apimgt.api.APIConsumer;
import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.model.API;
import barley.apimgt.api.model.APIIdentifier;
import barley.apimgt.api.model.APIKey;
import barley.apimgt.api.model.Application;
import barley.apimgt.api.model.Documentation;
import barley.apimgt.api.model.Documentation.DocumentSourceType;
import barley.apimgt.api.model.Documentation.DocumentVisibility;
import barley.apimgt.api.model.DocumentationType;
import barley.apimgt.api.model.ResourceFile;
import barley.apimgt.api.model.SubscribedAPI;
import barley.apimgt.api.model.Subscriber;
import barley.apimgt.api.model.Tier;
import barley.apimgt.api.model.policy.Policy;
import barley.apimgt.impl.factory.SQLConstantManagerFactory;
import barley.apimgt.impl.internal.APIManagerComponent;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.APIMgtDBUtil;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.impl.utils.KeyMgtProviderRestClient;
import barley.core.MultitenantConstants;
import barley.core.RegistryResources;
import barley.core.configuration.ServerConfiguration;
import barley.core.context.PrivilegedBarleyContext;
import barley.core.internal.OSGiDataHolder;
import barley.core.utils.CryptoException;
import barley.core.utils.CryptoUtil;
import barley.identity.core.util.IdentityConfigParser;
import barley.registry.core.Resource;
import barley.registry.core.exceptions.RegistryException;
import barley.registry.core.jdbc.EmbeddedRegistryService;
import barley.registry.core.service.TenantRegistryLoader;
import barley.registry.core.session.UserRegistry;
import barley.registry.indexing.service.TenantIndexingLoader;
import barley.user.api.ProfileConfigurationManager;
import barley.user.api.UserStoreException;
import barley.user.core.UserMgtConstants;
import barley.user.core.claim.Claim;
import barley.user.core.claim.ClaimMapping;
import barley.user.core.profile.ProfileConfiguration;
import barley.user.core.service.RealmService;

public class APICommonImplTest extends BaseTestCase {

	private static final Log log = LogFactory.getLog(APICommonImplTest.class);
    
    protected static EmbeddedRegistryService embeddedRegistryService = null;
    
    APIConsumer consumer;
    
    public static final String CLAIM_URI1 = "http://wso2.org/givenname";
    public static final String CLAIM_URI2 = "http://wso2.org2/givenname2";
    public static final String CLAIM_URI3 = "http://wso2.org/givenname3";
    
    public void setUp() throws Exception {
    	super.setUp();
    	
    	// 세션처리 
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);    	
        int tenantId = PrivilegedBarleyContext.getThreadLocalCarbonContext().getTenantId();
        
    	super.setUpCache();
    	setUpRegistry();
    	
    	//(오세창)
    	String baseDir = "D:\\Workspace_STS_SaaSPlatform\\barley.apimgt\\barley.apimgt.impl";
    	System.setProperty("APIManagerDBConfigurationPath", baseDir + "/src/test/resources/am-config.xml");
    	System.setProperty("IdentityConfigurationPath", baseDir + "/src/test/resources/identity.xml");
    	
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
    	
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
        
        APIUtil.loadTenantExternalStoreConfig(MultitenantConstants.SUPER_TENANT_ID);
        
        APIUtil.loadTenantConf(tenantId);
        APIUtil.loadTenantSelfSignUpConfigurations(tenantId);
        // subscription을 하기위해서는 워크플로를 로딩해야 한다. 초기에 /workflowextensions/workflow-extensions.xml에 읽어와 system registry에 저장 
        APIUtil.loadTenantWorkFlowExtensions(MultitenantConstants.SUPER_TENANT_ID);
        
        SQLConstantManagerFactory.initializeSQLConstantManager();
        
        consumer = APIManagerFactory.getInstance().getAPIConsumer("wso2.system.user@carbon.super");
        
        // 인증서 테스트를 위해 추가적으로 처리 
        OSGiDataHolder.getInstance().setServerConfigurationService(ServerConfiguration.getInstance());
        OSGiDataHolder.getInstance().setRegistryService(embeddedRegistryService);
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
    

    public void testGetAllApis() throws Exception {
    	String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	
    	List<API> apis = consumer.getAllAPIs();
    	assertNotNull(apis);
        assertTrue(apis.size() == 1);
        
        String searchQuery = "";
    	Map result = consumer.searchPaginatedAPIs(searchQuery, tenantDomain, 0, 10, false);
    }
    
    public void testGetApi() throws Exception {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	String uuid = "1dbcb372-f905-4c35-af19-0c36ee6d94d8";
    	
    	API api = consumer.getAPI(apiId);
    	assertNotNull(api);
    	
    	api = consumer.getLightweightAPI(apiId);
    	assertNotNull(api);
    	
    	api = consumer.getAPIbyUUID(uuid, tenantDomain);
    	assertNotNull(api);
    	
    	api = consumer.getLightweightAPIByUUID(uuid, tenantDomain);
    	assertNotNull(api);
    	
    	String apiPath = "/apimgt/applicationdata/provider/wso2.system.user-AT-carbon.super/WSO2Earth/2.0.0/api";
    	api = consumer.getAPI(apiPath);
    	assertNotNull(api);
    	
    	assertTrue(consumer.isAPIAvailable(apiId));
    	
    	String apiName = "WSO2Earth";
    	assertTrue(consumer.isApiNameExist(apiName));
    }

    public void testVersions() throws APIManagementException {
    	String userId = "wso2.system.user@carbon.super";
    	String apiName = "WSO2Earth";
    	Set<String> versions = consumer.getAPIVersions(userId, apiName);
    	assertEquals(1, versions.size());
    }
    
    public void testSwagger() throws APIManagementException {
    	// provider에서 swagger 정의 저장
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	String definition = consumer.getSwagger20Definition(apiId);
    	assertNotNull(definition);
    }
    
    public void testResourceFile() throws Exception {
    	String resourcePath = "/myresource/aa.txt";
    	FileInputStream content = new FileInputStream("D:\\temp\\aa.txt");
    	String contentType = "txt";
    	ResourceFile resourceFile = new ResourceFile(content, contentType); 
    	consumer.addResourceFile(resourcePath, resourceFile);
    }
    
    // REG_content 테이블의 regt_content_data에 값이 들어가 있음. (리소스 타입은 'application/vnd.wso2.registry-ext-type+xml')
    public void testDocument() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	Documentation doc = new Documentation(DocumentationType.HOWTO, "howto");    	
    	doc.setSummary("사용법 요약");
    	doc.setVisibility(DocumentVisibility.API_LEVEL);
    	doc.setSourceType(DocumentSourceType.INLINE);
    	
    	assertTrue(consumer.isDocumentationExist(apiId, "howto"));
    	
    	List<Documentation> docs = consumer.getAllDocumentation(apiId);
    	assertEquals(1, docs.size());
    	
    	// logedUserName null 이라도 동작함. 코드가 미완성 된 것으로 보임.
    	String logedUserName = "wso2.system.user@carbon.super";
    	docs = consumer.getAllDocumentation(apiId, logedUserName);
    	assertEquals(1, docs.size());
    	
//    	String requestedTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
//    	String docId = "89df3855-7a22-4e4e-9efa-b64ef0250efb";
//    	doc = consumer.getDocumentation(docId, requestedTenantDomain);
//    	assertNotNull(doc);
    	
    	String docName = "howto";
    	doc = consumer.getDocumentation(apiId, DocumentationType.HOWTO, docName);
    	assertNotNull(doc);
    	
    	String content = consumer.getDocumentationContent(apiId, docName);
    	assertNotNull(content);
    	
    }
    
    public void testSubscriber() throws APIManagementException {
    	String groupingId = null;    	
    	String userId = "wso2.system.user@carbon.super";
    	String applicationName = "DefaultApplication";
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	
//    	consumer.addSubscriber(userId, groupingId);
    	
//    	Subscriber subscriber = consumer.getSubscriber(userId);
//    	assertNotNull(subscriber);
//    	
//    	subscriber.setEmail("yangwenry@gmail.com");
//    	consumer.updateSubscriber(subscriber);
//    	
    	String accessToken = "2a2f04b50da753bbec5d611d8d0d66ee";
    	Subscriber sub = consumer.getSubscriberById(accessToken);
    	assertNotNull(sub);
    	
    	// 구현되어 있지 않음. 
//    	consumer.removeSubscriber(apiId, userId);
    	
//    	Set<API> apis = consumer.getSubscriberAPIs(subscriber);
//    	assertEquals(1, apis.size());
    }
    
    public void testApplicationKey() throws APIManagementException {
    	String accessToken = "ba0d60ba0bd4752f30719e82384b0915";
    	assertTrue(consumer.isApplicationTokenExists(accessToken));
    	
    	assertTrue(consumer.isApplicationTokenRevoked(accessToken));
    	
    	APIKey apiKey = consumer.getAccessTokenData(accessToken);
    	assertNotNull(apiKey);
    	
    	Set<APIIdentifier> apis = consumer.getAPIByAccessToken(accessToken);
    	assertEquals(1, apis.size());
    	
    	String searchType = null;
    	String searchTerm = "ba0d60ba0bd4752f3071";
    	String logedUserName = "wso2.system.user@carbon.super";
    	Map maps = consumer.searchAccessToken(searchType, searchTerm, logedUserName);
    	assertEquals(1, maps.size());
    }
    
    public void testContext() throws APIManagementException {
    	String context = "/wso2earth";
    	assertTrue(consumer.isContextExist(context));
    }
    
    // 에러발생 
    public void testScope() throws APIManagementException {
    	String scopeKey = "1234";
        int tenantId = -1234;        
        //assertTrue(consumer.isScopeKeyExist(scopeKey, tenantId));
        
        APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
        assertTrue(consumer.isScopeKeyAssigned(apiId, scopeKey, tenantId));
    }
    
    public void testIcon() throws APIManagementException {
    	APIIdentifier apiId = new APIIdentifier("wso2.system.user@carbon.super", "WSO2Earth", "2.0.0");
    	ResourceFile file = consumer.getIcon(apiId);
    	assertNull(file);
    }
    
    public void testSubscription() throws APIManagementException {
    	String uuid = "55ccb21a-7093-4a57-907b-c091181fab2d";
    	SubscribedAPI subscription = consumer.getSubscriptionByUUID(uuid);
    	assertNotNull(subscription);
    }
    
    public void testApplication() throws APIManagementException {
    	String userId = "wso2.system.user@carbon.super";
    	int applicationId = 6;
    	String groupId = "1";
    	
    	String uuid = "b8378b8b-18b9-457d-afb2-6fbd6e7dd0eb";
    	Application application = consumer.getApplicationByUUID(uuid);
    	assertNotNull(application);
    	
    	String accessToken = "";
    	assertTrue(consumer.isApplicationTokenExists(accessToken));
    }
    
    public void testTier() throws APIManagementException {
    	Set<Tier> tiers = consumer.getAllTiers();
    	assertEquals(5, tiers.size());
    	
    	String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	tiers = consumer.getAllTiers(tenantDomain);
    	assertEquals(5, tiers.size());
    	
    	tiers = consumer.getTiers();
    	assertEquals(4, tiers.size());
    	
    	tiers = consumer.getTiers(tenantDomain);
    	assertEquals(4, tiers.size());

    	String userId = "wso2.system.user@carbon.super";
    	tiers = consumer.getTiers(APIConstants.TIER_API_TYPE, userId);
    	assertEquals(4, tiers.size());
    }

    public void testEtc() throws APIManagementException {
    	String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    	String appType = "";
    	// Returns a map of gateway / store domains for the tenant
    	// registry에서 게이트웨이 및 스토어 도메인 주소를 리턴. 현재 도메인 주소를 저장한 데이터가 없음 
    	Map result = consumer.getTenantDomainMappings(tenantDomain, appType);
    	assertNotNull(result);
    	
    	String contextTemplate = "/wso2earth";
    	assertTrue(consumer.isDuplicateContextTemplate(contextTemplate));
    }
    
    public void testPolicy() throws APIManagementException {
    	String username = "wso2.system.user@carbon.super";
    	String level = "api";
    	// insertpolicy, updatepolicy
    	Policy[] policies = consumer.getPolicies(username, level);
    	assertEquals(3, policies.length);
    }
    
    public void testAddUser() throws Exception {
    	RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
    	int tenantId = -1234;
    	String userName = "wso2.system.user";
    	// password 
    	Object credential = new String("admin");
    	String[] roleList = {"internal/everyone"};
    	Map<String, String> claims = new HashMap<String, String>();
    	String profileName = "default";
    	//realmService.getBootstrapRealm().getUserStoreManager().addUser(userName, credential, roleList, claims, profileName);    	
    	realmService.getTenantUserRealm(tenantId).getUserStoreManager().addUser(userName, credential, roleList, claims, profileName);
    }
    
    public void testAddRole() throws Exception {
    	RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
    	int tenantId = 1;
    	
    	// um_role -> um_user_role 데이터 추가 - 사용자에게 롤을 부여 
//    	String roleName = "internal/everyone";
//    	String[] userList = {"wso2.system.user"};
    	String roleName = "everyone";
    	String[] userList = {"wso2.anonymous.user"};
    	
//    	Permission[] permissions = new Permission[] {
//    		new Permission(APIConstants.Permissions.API_CREATE, UserMgtConstants.EXECUTE_ACTION)
//    	};
//    	realmService.getTenantUserRealm(tenantId).getUserStoreManager().addRole(roleName, userList, permissions);
    	
    	// um_role_permission => um_permission 테이블 데이터 추가. 즉, 롤별 권한 부여 
//    	String roleName = "admin";
//    	String roleName = "wso2.anonymous.role";	// um_user_role에서 um_role 테이블을 조회하면 사용자의 롤을 조회할 수 있음.
    	String action = UserMgtConstants.EXECUTE_ACTION;
//    	String resourceId = APIConstants.Permissions.API_CREATE;
//    	String resourceId = APIConstants.Permissions.API_PUBLISH;
    	String resourceId = APIConstants.Permissions.API_SUBSCRIBE;
//    	String resourceId = APIConstants.Permissions.API_CREATE;
//    	realmService.getTenantUserRealm(tenantId).getAuthorizationManager().authorizeRole(roleName, resourceId, action);
    	
    	// um_user_role
    	String userName = "wso2.anonymous.user";
    	String[] deletedRoles = null;
    	String[] newRoles = {"everyone"};
    	realmService.getTenantUserRealm(tenantId).getUserStoreManager().updateRoleListOfUser(userName, deletedRoles, newRoles);
    	
    	// um_user_permission => um_permission
    	// 디프리케이트된 이유를 알겠음. 사용자는 롤별로 움직이기 때문에 굳이 사용자에게 퍼미션을 추가해 줄 필요가 없다. (굳이 사용하지 말자)
    	//String userName = "yangwenry";
//    	String userName = "wso2.anonymous.use";
//    	realmService.getTenantUserRealm(tenantId).getAuthorizationManager().authorizeUser(userName, resourceId, action);
    	
    }
    
    // um_dialect => um_claim
    public void testAddDialect() throws UserStoreException {
    	RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
    	int tenantId = 1;
    	ClaimMapping mapping = new ClaimMapping();
    	/*Claim claim1 = new Claim();
    	claim1.setClaimUri("http://wso2.org/claims/grpauthcode");
    	claim1.setDescription("권한코드");
    	claim1.setDialectURI("http://wso2.org/claims");
    	claim1.setDisplayTag("Group Auth Code");
    	claim1.setRequired(true);
    	claim1.setSupportedByDefault(false);
    	mapping.setMappedAttribute("grpAuthCode");
    	mapping.setClaim(claim1);*/
    	
    	Claim claim2 = new Claim();
    	claim2.setClaimUri("http://wso2.org/claims/menugrpcode");
    	claim2.setDescription("메뉴권한코드");
    	claim2.setDialectURI("http://wso2.org/claims");
    	claim2.setDisplayTag("Menu Group Code");
    	claim2.setRequired(true);
    	claim2.setSupportedByDefault(false);
    	mapping.setMappedAttribute("menuGrpCd");
    	mapping.setClaim(claim2);
    	
    	realmService.getTenantUserRealm(tenantId).getClaimManager().addNewClaimMapping(mapping);
//    	realmService.getTenantUserRealm(tenantId).getClaimManager().deleteClaimMapping(mapping);
    }
    
    // um_profile_config => um_claim_behavior
    // 버그: um_profile_config 테이블에 데이터가 계속 들어간다. 이미 데이터가 있으니 들어갈 이유가 없다.
    public void testUserProfile() throws UserStoreException {
    	int tenantId = 1;
    	RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
    	ProfileConfigurationManager pcm = realmService.getTenantUserRealm(tenantId).getProfileConfigurationManager();
    	
    	String[] claimset = {CLAIM_URI1, CLAIM_URI2, CLAIM_URI3};
//    	String claimURI = "http://wso2.org/claims/grpauthcode";
//    	String claimURI = "http://wso2.org/claims/menugrpcode";
    	String claimURI = "http://wso2.org/claims/fullname";
    	
    	ProfileConfiguration p4 = new ProfileConfiguration();
        p4.setProfileName("default");
        p4.addHiddenClaim(null);
        p4.addInheritedClaim(null);
//        p4.addOverriddenClaim(claimset[2]);
        p4.addInheritedClaim(claimURI);
        p4.setDialectName("http://wso2.org/claims");
    	
    	//add profile configuration
        pcm.addProfileConfig(p4);
        
//        pcm.updateProfileConfig(p4);
//        pcm.deleteProfileConfig(p4);
        
//        ProfileConfiguration[] profiles = pcm.getAllProfiles();
//    	for(ProfileConfiguration profile : profiles) {
//    		System.out.println(profile);
//    	}
    }
    
    public void testChangeUserClaimValue() throws Exception {
    	RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
    	int tenantId = 1;
    	String profileName = "default";
    	
    	String userName = "user01";
//    	String claimURI = "http://wso2.org/claims/fullname";
//    	String chanagedValue = "오세창";
    	
//    	String claimURI = "http://wso2.org/claims/grpauthcode";
//    	String chanagedValue = "S0001";
    	
    	String claimURI = "http://wso2.org/claims/menugrpcode";
    	String chanagedValue = "SYSTEM_MANAGER";
    	
    	realmService.getTenantUserRealm(tenantId).getUserStoreManager().setUserClaimValue(userName, claimURI, chanagedValue, profileName);
    	String myName = realmService.getTenantUserRealm(tenantId).getUserStoreManager().getUserClaimValue(userName, claimURI, profileName);
    	assertEquals(chanagedValue, myName);
    }
    
    
    public void testKeyMgtProvider() throws APIManagementException {    	
    	KeyMgtProviderRestClient client = new KeyMgtProviderRestClient();
    	String[] consumerKeys = {"K35aQEqgIyBpC1_GC0erfp0scvUa"};
    	client.removeScopeCache(consumerKeys);
    }
    
    public void testGetRegistry() throws RegistryException {
    	int tenantID = 1; 
    	UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry("wso2.anonymous.user", tenantID);
    	String swaggerPath = "/apimgt/applicationdata/provider/admin-AT-codefarm.co.kr/pub1/v3/";
		String url = swaggerPath + "swagger.json";
		
		Resource data = registry.get(url);
		assertNotNull(data);
    }
    
    public void testAddSingKeyToRegistry() throws RegistryException, FileNotFoundException, CryptoException {
    	// 인증서 등록을 위해서 프로바이더를 등록해야 한다.
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    	
//    	String userName = "admin";
//    	UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().getRegistry(userName);
    	int tenantId = 1;
    	UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
    	
    	Resource signKeyReg = registry.newResource();
    	String filePath = "D:\\Workspace_STS_SaaSPlatform\\sso.server\\src\\main\\resources\\security\\codefarm-co-kr.jks";
        FileInputStream input = new FileInputStream(filePath);
        signKeyReg.setContent(input);
        signKeyReg.setDescription("코드팜 인증서 파일");
        signKeyReg.setProperty(RegistryResources.SecurityManagement.PROP_TYPE, "jks");
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        // registry에 저장하는 비번과 생성된 인증서(codefarm-co-kr.jks) 비밀번호가 동일해야 한다. 
        String password = new String(cryptoUtil.encryptAndBase64Encode("zhemvka".getBytes()));
        signKeyReg.setProperty(RegistryResources.SecurityManagement.PROP_PASSWORD, password);
        signKeyReg.setProperty(RegistryResources.SecurityManagement.PROP_PRIVATE_KEY_PASS, password);
        String signKeyRegPath = "/repository/security/key-stores/codefarm-co-kr.jks";
        registry.put(signKeyRegPath, signKeyReg);
        
        signKeyReg.discard();
    }
    
    public void testRemoveSignKeyFromRegistry() throws RegistryException {
    	String path = "/repository/security/key-stores/codefarm-co-kr.jks";
//    	String username = "admin";
//    	embeddedRegistryService.getUserRegistry(username).delete(path);
    	
    	int tenantId = 1;
    	UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
    	registry.delete(path);
    	
    	// 프로퍼티 삭제 
    	
    }
}



 