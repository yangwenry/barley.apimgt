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

package barley.apimgt.impl.template;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
//import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import barley.apimgt.api.model.API;
import barley.apimgt.impl.APIConstants;
/**
 * Set the parameters for secured endpoints
 */
public class SecurityConfigContext extends ConfigContextDecorator {

    private API api;

    public SecurityConfigContext(ConfigContext context,API api) {
        super(context);
        this.api = api;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        String alias =  api.getId().getProviderName() + "--" + api.getId().getApiName()
                        + api.getId().getVersion();
        String unpw = api.getEndpointUTUsername() + ":" + api.getEndpointUTPassword();

        // (임의주석)
//        boolean isSecureVaultEnabled = Boolean.parseBoolean(ServiceReferenceHolder.getInstance().
//                                                     getAPIManagerConfigurationService().getAPIManagerConfiguration().
//                                                     getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));
        boolean isSecureVaultEnabled = false;
        
        context.put("isEndpointSecured", api.isEndpointSecured());
        context.put("isEndpointAuthDigest", api.isEndpointAuthDigest());
        context.put("username", api.getEndpointUTUsername());
        context.put("securevault_alias", alias);
        context.put("base64unpw", new String(Base64.encodeBase64(unpw.getBytes())));
        context.put("isSecureVaultEnabled", isSecureVaultEnabled);
        
        return context;
    }

}
