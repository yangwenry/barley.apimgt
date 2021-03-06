/*
*Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package barley.apimgt.keymgt.token;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.KeyStoreManager;

import com.nimbusds.jwt.JWTClaimsSet;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.token.ClaimsRetriever;
import barley.apimgt.impl.utils.APIUtil;
import barley.apimgt.keymgt.service.TokenValidationContext;
import barley.core.MultitenantConstants;
import barley.core.multitenancy.MultitenantUtils;
import barley.registry.core.exceptions.RegistryException;
import barley.user.api.RealmConfiguration;
import barley.user.api.UserStoreException;
import barley.user.core.UserStoreManager;
import barley.user.core.service.RealmService;

/**
 * This class represents the JSON Web Token generator.
 * By default the following properties are encoded to each authenticated API request:
 * subscriber, applicationName, apiContext, version, tier, and endUserName
 * Additional properties can be encoded by engaging the ClaimsRetrieverImplClass callback-handler.
 * The JWT header and body are base64 encoded separately and concatenated with a dot.
 * Finally the token is signed using SHA256 with RSA algorithm.
 */
public abstract class AbstractJWTGenerator implements TokenGenerator {

    private static final Log log = LogFactory.getLog(AbstractJWTGenerator.class);

    public static final String API_GATEWAY_ID = "wso2.org/products/am";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String NONE = "NONE";

    private static volatile long ttl = -1L;

    private ClaimsRetriever claimsRetriever;

    private String dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;

    private String signatureAlgorithm = SHA256_WITH_RSA;

    private static ConcurrentHashMap<Integer, Key> privateKeys = new ConcurrentHashMap<Integer, Key>();
    private static ConcurrentHashMap<Integer, Certificate> publicCerts = new ConcurrentHashMap<Integer, Certificate>();

    private String userAttributeSeparator = APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;

    public AbstractJWTGenerator() {

        dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.CONSUMER_DIALECT_URI);
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }
        signatureAlgorithm = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.JWT_SIGNATURE_ALGORITHM);
        if (signatureAlgorithm == null || !(NONE.equals(signatureAlgorithm)
                                            || SHA256_WITH_RSA.equals(signatureAlgorithm))) {
            signatureAlgorithm = SHA256_WITH_RSA;
        }

        String claimsRetrieverImplClass =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(APIConstants.CLAIMS_RETRIEVER_CLASS);

        if (claimsRetrieverImplClass != null) {
            try {
                claimsRetriever = (ClaimsRetriever) APIUtil.getClassForName(claimsRetrieverImplClass).newInstance();
                claimsRetriever.init();
            } catch (ClassNotFoundException e) {
                log.error("Cannot find class: " + claimsRetrieverImplClass, e);
            } catch (InstantiationException e) {
                log.error("Error instantiating " + claimsRetrieverImplClass, e);
            } catch (IllegalAccessException e) {
                log.error("Illegal access to " + claimsRetrieverImplClass, e);
            } catch (APIManagementException e) {
                log.error("Error while initializing " + claimsRetrieverImplClass, e);
            }
        }
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public ClaimsRetriever getClaimsRetriever() {
        return claimsRetriever;
    }

    public abstract Map<String, String> populateStandardClaims(TokenValidationContext validationContext)
            throws APIManagementException;

    public abstract Map<String, String> populateCustomClaims(TokenValidationContext validationContext) throws APIManagementException;

    public String encode(byte[] stringToBeEncoded) throws APIManagementException {
        return Base64Utils.encode(stringToBeEncoded);
    }

    public String generateToken(TokenValidationContext validationContext) throws APIManagementException{

        String jwtHeader = buildHeader(validationContext);

        String base64UrlEncodedHeader = "";
        if (jwtHeader != null) {
            base64UrlEncodedHeader = encode(jwtHeader.getBytes(Charset.defaultCharset()));
        }

        String jwtBody = buildBody(validationContext);
        String base64UrlEncodedBody = "";
        if (jwtBody != null) {
            base64UrlEncodedBody = encode(jwtBody.getBytes());
        }

        if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            String assertion = base64UrlEncodedHeader + '.' + base64UrlEncodedBody;

            //get the assertion signed
            byte[] signedAssertion = signJWT(assertion, validationContext.getValidationInfoDTO().getEndUserName());

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion, Charset.defaultCharset()));
            }
            String base64UrlEncodedAssertion = encode(signedAssertion);

            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.' + base64UrlEncodedAssertion;
        } else {
            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.';
        }
    }


    public String buildHeader(TokenValidationContext tokenValidationContext) throws APIManagementException {
        String jwtHeader = null;

        //if signature algo==NONE, header without cert
        if (NONE.equals(signatureAlgorithm)) {
            StringBuilder jwtHeaderBuilder = new StringBuilder();
            jwtHeaderBuilder.append("{\"typ\":\"JWT\",");
            jwtHeaderBuilder.append("\"alg\":\"");
            jwtHeaderBuilder.append(getJWSCompliantAlgorithmCode(NONE));
            jwtHeaderBuilder.append('\"');
            jwtHeaderBuilder.append('}');

            jwtHeader = jwtHeaderBuilder.toString();

        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            jwtHeader = addCertToHeader(tokenValidationContext.getValidationInfoDTO().getEndUserName());
        }
        return jwtHeader;
    }

    public String buildBody(TokenValidationContext validationContext) throws APIManagementException {

        Map<String, String> standardClaims = populateStandardClaims(validationContext);
        Map<String, String> customClaims = populateCustomClaims(validationContext);

        //get tenantId
        int tenantId = APIUtil.getTenantId(validationContext.getValidationInfoDTO().getEndUserName());

        String claimSeparator = getMultiAttributeSeparator(tenantId);
        if (StringUtils.isNotBlank(claimSeparator)) {
            userAttributeSeparator = claimSeparator;
        }

        if (standardClaims != null) {
            if (customClaims != null) {
                standardClaims.putAll(customClaims);
            }

            Map<String, Object> claims = new HashMap<String, Object>();
            JWTClaimsSet claimsSet = new JWTClaimsSet();

            if(standardClaims != null) {
                Iterator<String> it = new TreeSet(standardClaims.keySet()).iterator();
                while (it.hasNext()) {
                    String claimURI = it.next();
                    String claimVal = standardClaims.get(claimURI);
                    List<String> claimList = new ArrayList<String>();
                    if (userAttributeSeparator != null && claimVal != null && claimVal.contains(userAttributeSeparator)) {
                        StringTokenizer st = new StringTokenizer(claimVal, userAttributeSeparator);
                        while (st.hasMoreElements()) {
                            String attValue = st.nextElement().toString();
                            if (StringUtils.isNotBlank(attValue)) {
                                claimList.add(attValue);
                            }
                        }
                        claims.put(claimURI, claimList.toArray(new String[claimList.size()]));
                    } else if ("exp".equals(claimURI)) {
                        claims.put("exp", new Date(Long.valueOf(standardClaims.get(claimURI))));
                    } else {
                        claims.put(claimURI, claimVal);
                    }
                }
            }

            claimsSet.setAllClaims(claims);
            return claimsSet.toJSONObject().toJSONString();
        }
        return null;
    }


    private byte[] signJWT(String assertion, String endUserName) throws APIManagementException {

        String tenantDomain = null;

        try {
            //get tenant domain
            tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = APIUtil.getTenantId(endUserName);

            Key privateKey = null;

            if (!(privateKeys.containsKey(tenantId))) {
                APIUtil.loadTenantRegistry(tenantId);
                //get tenant's key store manager
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //derive key store name
                    String ksName = tenantDomain.trim().replace('.', '-');
                    String jksName = ksName + ".jks";
                    //obtain private key
                    //TODO: maintain a hash map with tenants' private keys after first initialization
                    privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);
                } else {
                    try {
                        privateKey = tenantKSM.getDefaultPrivateKey();
                    } catch (Exception e) {
                        log.error("Error while obtaining private key for super tenant", e);
                    }
                }
                if (privateKey != null) {
                    privateKeys.put(tenantId, privateKey);
                }
            } else {
                privateKey = privateKeys.get(tenantId);
            }

            //initialize signature with private key and algorithm
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign((PrivateKey) privateKey);

            //update signature with data to be signed
            byte[] dataInBytes = assertion.getBytes(Charset.defaultCharset());
            signature.update(dataInBytes);

            //sign the assertion and return the signature
            return signature.sign();

        } catch (NoSuchAlgorithmException e) {
            String error = "Signature algorithm not found.";
            //do not log
            throw new APIManagementException(error, e);
        } catch (InvalidKeyException e) {
            String error = "Invalid private key provided for the signature";
            //do not log
            throw new APIManagementException(error, e);
        } catch (SignatureException e) {
            String error = "Error in signature";
            //do not log
            throw new APIManagementException(error, e);
        } catch (RegistryException e) {
            String error = "Error in loading tenant registry for " + tenantDomain;
            //do not log
            throw new APIManagementException(error, e);
        }
    }

    protected long getTTL() {
        if (ttl != -1) {
            return ttl;
        }

        synchronized (JWTGenerator.class) {
            if (ttl != -1) {
                return ttl;
            }
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();

            String gwTokenCacheConfig = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            boolean isGWTokenCacheEnabled = Boolean.parseBoolean(gwTokenCacheConfig);

            String kmTokenCacheConfig = config.getFirstProperty(APIConstants.KEY_MANAGER_TOKEN_CACHE);
            boolean isKMTokenCacheEnabled = Boolean.parseBoolean(kmTokenCacheConfig);

            if (isGWTokenCacheEnabled || isKMTokenCacheEnabled) {
                String apimKeyCacheExpiry = config.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);

                if (apimKeyCacheExpiry != null) {
                    ttl = Long.parseLong(apimKeyCacheExpiry);
                }
            }
            else {
                String ttlValue = config.getFirstProperty(APIConstants.JWT_EXPIRY_TIME);
                if (ttlValue != null) {
                    ttl = Long.parseLong(ttlValue);
                } else {
                    //15 * 60 (convert 15 minutes to seconds)
                    ttl = Long.valueOf(900);
                }
            }
            return ttl;
        }
    }

    /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param endUserName - The end user name
     * @throws APIManagementException
     */
    private String addCertToHeader(String endUserName) throws APIManagementException {

        try {
            //get tenant domain
            String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = APIUtil.getTenantId(endUserName);
            Certificate publicCert;

            if (!(publicCerts.containsKey(tenantId))) {
                //get tenant's key store manager
                APIUtil.loadTenantRegistry(tenantId);
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                KeyStore keyStore;
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //derive key store name
                    String ksName = tenantDomain.trim().replace('.', '-');
                    String jksName = ksName + ".jks";
                    keyStore = tenantKSM.getKeyStore(jksName);
                    publicCert = keyStore.getCertificate(tenantDomain);
                } else {
                    //keyStore = tenantKSM.getPrimaryKeyStore();
                    publicCert = tenantKSM.getDefaultPrimaryCertificate();
                }
                if (publicCert != null) {
                    publicCerts.put(tenantId, publicCert);
                }
            } else {
                publicCert = publicCerts.get(tenantId);
            }

            //generate the SHA-1 thumbprint of the certificate
            //TODO: maintain a hashmap with tenants' pubkey thumbprints after first initialization
            MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
            if (publicCert != null) {
                byte[] der = publicCert.getEncoded();
                digestValue.update(der);
                byte[] digestInBytes = digestValue.digest();
                Base64  base64 = new Base64(true);
                String base64UrlEncodedThumbPrint = base64.encodeToString(digestInBytes).trim();
                StringBuilder jwtHeader = new StringBuilder();
                //Sample header
                //{"typ":"JWT", "alg":"SHA256withRSA", "x5t":"a_jhNus21KVuoFx65LmkW2O_l10"}
                //{"typ":"JWT", "alg":"[2]", "x5t":"[1]"}
                jwtHeader.append("{\"typ\":\"JWT\",");
                jwtHeader.append("\"alg\":\"");
                jwtHeader.append(getJWSCompliantAlgorithmCode(signatureAlgorithm));
                jwtHeader.append("\",");

                jwtHeader.append("\"x5t\":\"");
                jwtHeader.append(base64UrlEncodedThumbPrint);
                jwtHeader.append('\"');

                jwtHeader.append('}');
                return jwtHeader.toString();
            } else {
                String error = "Error in obtaining tenant's keystore";
                throw new APIManagementException(error);
            }

        } catch (KeyStoreException e) {
            String error = "Error in obtaining tenant's keystore";
            throw new APIManagementException(error, e);
        } catch (CertificateEncodingException e) {
            String error = "Error in generating public cert thumbprint";
            throw new APIManagementException(error, e);
        } catch (NoSuchAlgorithmException e) {
            String error = "Error in generating public cert thumbprint";
            throw new APIManagementException(error, e);
        } catch (Exception e) {
            String error = "Error in obtaining tenant's keystore";
            throw new APIManagementException(error, e);
        }
    }

    /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes - The input byte array
     * @return hexadecimal representation
     */
    private String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }

        return buf.toString();
    }

    /**
     * Get the JWS compliant signature algorithm code of the algorithm used to sign the JWT.
     * @param signatureAlgorithm - The algorithm used to sign the JWT. If signing is disabled, the value will be NONE.
     * @return - The JWS Compliant algorithm code of the signature algorithm.
     */
    public String getJWSCompliantAlgorithmCode(String signatureAlgorithm){

        if (signatureAlgorithm == null || NONE.equals(signatureAlgorithm)){
            return JWTSignatureAlg.NONE.getJwsCompliantCode();
        }
        else if(SHA256_WITH_RSA.equals(signatureAlgorithm)){
            return JWTSignatureAlg.SHA256_WITH_RSA.getJwsCompliantCode();
        }
        else{
            return signatureAlgorithm;
        }
    }

    private String getMultiAttributeSeparator(int tenantId) {
        try {
            RealmConfiguration realmConfiguration = null;
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

            if (realmService != null && tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                UserStoreManager userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(tenantId).getUserStoreManager();


                realmConfiguration = userStoreManager.getRealmConfiguration();
            }

            if (realmConfiguration != null) {
                String claimSeparator = realmConfiguration.getUserStoreProperty(APIConstants.MULTI_ATTRIBUTE_SEPARATOR);
                if (claimSeparator != null && !claimSeparator.trim().isEmpty()) {
                    return claimSeparator;
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while getting the realm configuration, User store properties might not be " +
                      "returned", e);
        }
        return null;
    }
}
