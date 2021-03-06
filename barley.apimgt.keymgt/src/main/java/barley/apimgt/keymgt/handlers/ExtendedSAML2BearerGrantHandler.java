/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package barley.apimgt.keymgt.handlers;


import barley.apimgt.keymgt.ScopesIssuer;
import barley.identity.oauth2.token.OAuthTokenReqMessageContext;
import barley.identity.oauth2.token.handlers.grant.saml.SAML2BearerGrantHandler;


public class ExtendedSAML2BearerGrantHandler extends SAML2BearerGrantHandler {

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) {
        return ScopesIssuer.getInstance().setScopes(tokReqMsgCtx);
    }
}
