/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package barley.apimgt.impl.factory;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.api.APIManagementException;
import barley.apimgt.api.model.KeyManager;
import barley.apimgt.api.model.KeyManagerConfiguration;
import barley.apimgt.impl.AMDefaultKeyManagerImpl;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.APIManagerConfiguration;
import barley.apimgt.impl.DefaultKeyManagerImpl;
import barley.apimgt.impl.utils.APIUtil;

/**
 * This is a factory class.you have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiate from here.
 */
public class KeyManagerHolder {


    private static Log log = LogFactory.getLog(KeyManagerHolder.class);
    private static KeyManager keyManager = null;


    /**
     * Read values from APIManagerConfiguration.
     *
     * @param apiManagerConfiguration API Manager Configuration
     * @throws APIManagementException
     */
    public static void initializeKeyManager(APIManagerConfiguration apiManagerConfiguration)
            throws APIManagementException {
        if (apiManagerConfiguration != null) {
            try {
                // If APIKeyManager section is disabled, we are reading values defined in APIKeyValidator section.
                if (apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT) == null) {
                    //keyManager = (KeyManager) Class.forName("org.wso2.carbon.apimgt.keymgt.AMDefaultKeyManagerImpl").newInstance();
                	
                	// (수정) AM은 웹서비스 stub을 사용하므로 Default를 만들어 Http로 통신하게끔 변경한다.
                    //keyManager = new AMDefaultKeyManagerImpl();
                	keyManager = new DefaultKeyManagerImpl();
                    keyManager.loadConfiguration(null);
                } else {
                    // If APIKeyManager section is enabled, class name is picked from there.
                    String clazz = apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT);
                    keyManager = (KeyManager) APIUtil.getClassForName(clazz).newInstance();
                    Set<String> configKeySet = apiManagerConfiguration.getConfigKeySet();

                    KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();

                    // Iterating through the Configuration and seeing which elements are starting with APIKeyManager
                    // .Configuration. Values of those keys will be set in KeyManagerConfiguration object.
                    String startKey = APIConstants.API_KEY_MANAGER + "Configuration.";
                    for (String configKey : configKeySet) {
                        if (configKey.startsWith(startKey)) {
                            keyManagerConfiguration.addParameter(configKey.replace(startKey, ""),
                                                                 apiManagerConfiguration.getFirstProperty(configKey));
                        }
                    }

                    // Set the created configuration in the KeyManager instance.
                    keyManager.loadConfiguration(keyManagerConfiguration);
                }
            } catch (ClassNotFoundException e) {
                log.error("Error occurred while instantiating KeyManager implementation");
                throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
            } catch (InstantiationException e) {
                log.error("Error occurred while instantiating KeyManager implementation");
                throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
            } catch (IllegalAccessException e) {
                log.error("Error occurred while instantiating KeyManager implementation");
                throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
            }
        }
    }

    /**
     * This method will take hardcoded class name from api-manager.xml file and will return that class's instance.
     * This class should be implementation class of keyManager.
     *
     * @return keyManager instance.
     */
    public static KeyManager getKeyManagerInstance() {
        return keyManager;
    }

}
