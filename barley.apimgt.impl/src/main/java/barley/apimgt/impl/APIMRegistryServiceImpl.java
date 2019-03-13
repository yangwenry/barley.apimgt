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

import java.nio.charset.Charset;


import barley.apimgt.impl.internal.ServiceReferenceHolder;
import barley.apimgt.impl.utils.APIUtil;
import barley.core.MultitenantConstants;
import barley.core.context.PrivilegedBarleyContext;
import barley.registry.core.Registry;
import barley.registry.core.Resource;
import barley.registry.core.exceptions.RegistryException;
import barley.user.api.UserStoreException;

public class APIMRegistryServiceImpl implements APIMRegistryService {
    @Override
    public String getConfigRegistryResourceContent(String tenantDomain, final String registryLocation)
                                        throws UserStoreException, RegistryException {
        String content = null;
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            PrivilegedBarleyContext.startTenantFlow();
            PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
            APIUtil.loadTenantRegistry(tenantId);

            if (registry.resourceExists(registryLocation)) {
                Resource resource = registry.get(registryLocation);
                content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            }
        }
        finally {
            PrivilegedBarleyContext.endTenantFlow();
        }

        return content;
    }

    @Override
    public String getGovernanceRegistryResourceContent(String tenantDomain, String registryLocation)
                                        throws UserStoreException, RegistryException {
        String content = null;
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            PrivilegedBarleyContext.startTenantFlow();
            PrivilegedBarleyContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);

            if (registry.resourceExists(registryLocation)) {
                Resource resource = registry.get(registryLocation);
                content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            }
        }
        finally {
            PrivilegedBarleyContext.endTenantFlow();
        }

        return content;
    }
}
