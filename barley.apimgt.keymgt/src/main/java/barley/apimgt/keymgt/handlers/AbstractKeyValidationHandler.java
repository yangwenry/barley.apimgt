/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package barley.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.model.AccessTokenInfo;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.dao.ApiMgtDAO;
import barley.apimgt.impl.dto.APIKeyValidationInfoDTO;
import barley.apimgt.keymgt.APIKeyMgtException;
import barley.apimgt.keymgt.service.TokenValidationContext;
import barley.apimgt.keymgt.token.TokenGenerator;
import barley.apimgt.keymgt.util.APIKeyMgtDataHolder;

public abstract class AbstractKeyValidationHandler implements KeyValidationHandler {

    private static final Log log = LogFactory.getLog(AbstractKeyValidationHandler.class);
    private ApiMgtDAO dao = ApiMgtDAO.getInstance();

    @Override
    public boolean validateSubscription(TokenValidationContext validationContext) throws APIKeyMgtException {

        if (validationContext == null || validationContext.getValidationInfoDTO() == null) {
            return false;
        }

        if (validationContext.isCacheHit()) {
            return true;
        }

        APIKeyValidationInfoDTO dto = validationContext.getValidationInfoDTO();


        if (validationContext.getTokenInfo() != null) {
            if (validationContext.getTokenInfo().isApplicationToken()) {
                dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            } else {
                dto.setUserType("APPLICATION_USER");
            }

            AccessTokenInfo tokenInfo = validationContext.getTokenInfo();

            // This block checks if a Token of Application Type is trying to access a resource protected with
            // Application Token
            if (!hasTokenRequiredAuthLevel(validationContext.getRequiredAuthenticationLevel(), tokenInfo)) {
                dto.setAuthorized(false);
                dto.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE);
                return false;
            }
        }

        boolean state = false;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Before validating subscriptions : " + dto);
                log.debug("Validation Info : { context : " + validationContext.getContext() + " , " + "version : "
                        + validationContext.getVersion() + " , consumerKey : " + dto.getConsumerKey() + " }");
            }

            state = dao.validateSubscriptionDetails(validationContext.getContext(), validationContext.getVersion(),
                    dto.getConsumerKey(), dto);
//            if (state) {
//                dto.setAuthorizedDomains(APIUtil
//                        .getListOfAuthorizedDomainsByConsumerKey(validationContext.getTokenInfo().getConsumerKey()));
//            }


            if (log.isDebugEnabled()) {
                log.debug("After validating subscriptions : " + dto);
            }


        } catch (APIManagementException e) {
            log.error("Error Occurred while validating subscription.", e);
        }

        return state;
    }

    /**
     * Determines whether the provided token is an ApplicationToken.
     *
     * @param tokenInfo - Access Token Information
     */
    protected void setTokenType(AccessTokenInfo tokenInfo) {


    }

    /**
     * Resources protected with Application token type can only be accessed using Application Access Tokens. This method
     * verifies if a particular resource can be accessed using the obtained token.
     *
     * @param authScheme Type of token required by the resource (Application | User Token)
     * @param tokenInfo  Details about the Token
     * @return {@code true} if token is of the type required, {@code false} otherwise.
     */
    protected boolean hasTokenRequiredAuthLevel(String authScheme,
                                                AccessTokenInfo tokenInfo) {

        if (authScheme == null || authScheme.isEmpty() || tokenInfo == null) {
            return false;
        }

        if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authScheme)) {
            return tokenInfo.isApplicationToken();
        } else if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authScheme)) {
            return !tokenInfo.isApplicationToken();
        }

        return true;

    }

    @Override
    public boolean generateConsumerToken(TokenValidationContext validationContext) throws APIKeyMgtException {

      TokenGenerator generator = APIKeyMgtDataHolder.getTokenGenerator();

        try {
            String jwt = generator.generateToken(validationContext);
            validationContext.getValidationInfoDTO().setEndUserToken(jwt);
            return true;

        } catch (APIManagementException e) {
            log.error("Error occurred while generating JWT. ", e);
        }

        return false;
    }

}
