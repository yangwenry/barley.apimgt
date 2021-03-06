/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package barley.apimgt.gateway.throttling.util;


import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import barley.apimgt.gateway.internal.ServiceReferenceHolder;
import barley.apimgt.gateway.utils.GatewayUtils;
import barley.apimgt.impl.dto.ThrottleProperties;
import barley.apimgt.impl.utils.APIUtil;

public class KeyTemplateRetriever extends TimerTask {
    private static final Log log = LogFactory.getLog(KeyTemplateRetriever.class);
    private static final int keyTemplateRetrievalTimeoutInSeconds = 15;
    private static final int keyTemplateRetrievalRetries = 15;

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Starting web service based block condition data retrieving process.");
        }
        loadKeyTemplatesFromWebService();
    }


    /**
     * This method will retrieve KeyTemplates
     *
     * @return String object array which contains Blocking conditions.
     */
    private String[] retrieveKeyTemplateData() {

        try {
            ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = ServiceReferenceHolder
                    .getInstance().getThrottleProperties().getBlockCondition();
            String url = blockConditionRetrieverConfiguration.getServiceUrl() + "/keyTemplates";
            byte[] credentials = Base64.encodeBase64((blockConditionRetrieverConfiguration.getUsername() + ":" +
                                                      blockConditionRetrieverConfiguration.getPassword()).getBytes
                    (StandardCharsets.UTF_8));
            HttpGet method = new HttpGet(url);
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL keyMgtURL = new URL(url);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    retry = false;
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < keyTemplateRetrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving throttling data from remote endpoint: " + ex.getMessage()
                                 + ". Retrying after " + keyTemplateRetrievalTimeoutInSeconds + " seconds...");
                        Thread.sleep(keyTemplateRetrievalTimeoutInSeconds * 1000);
                    } else {
                        throw ex;
                    }
                }
            } while(retry);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                JSONArray jsonArray = (JSONArray) new JSONParser().parse(responseString);
                return (String[]) jsonArray.toArray(new String[jsonArray.size()]);
            }
        } catch (IOException | InterruptedException | ParseException e) {
            log.error("Exception when retrieving throttling data from remote endpoint ", e);
        }
        return null;
    }


    public void loadKeyTemplatesFromWebService() {
        List keyListMap = Arrays.asList(retrieveKeyTemplateData());
        ServiceReferenceHolder.getInstance().getThrottleDataHolder().addKeyTemplateFromMap(GatewayUtils.generateMap(keyListMap));
    }

    public void startKeyTemplateDataRetriever() {
        new Timer().schedule(this, ServiceReferenceHolder
                .getInstance().getThrottleProperties().getBlockCondition().getInitDelay());
    }


}
