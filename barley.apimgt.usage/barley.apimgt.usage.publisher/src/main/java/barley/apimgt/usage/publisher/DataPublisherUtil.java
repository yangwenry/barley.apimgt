/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package barley.apimgt.usage.publisher;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import barley.apimgt.impl.APIManagerAnalyticsConfiguration;
import barley.apimgt.usage.publisher.internal.UsageComponent;
import barley.core.configuration.ServerConfiguration;

public class DataPublisherUtil {

    private static final Log log = LogFactory
            .getLog(DataPublisherUtil.class);

    private static String hostAddress = null;
    public static final String HOST_NAME = "HostName";
    private static final String UNKNOWN_HOST = "UNKNOWN_HOST";
    private static boolean isEnabledMetering=false;
    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    public static String getHostAddress() {

        if (hostAddress != null) {
            return hostAddress;
        }
        hostAddress =   ServerConfiguration.getInstance().getFirstProperty(HOST_NAME);
        if(null == hostAddress){
        	if (getLocalAddress() != null) {
        		hostAddress = getLocalAddress().getHostName();
        	}
            if (hostAddress == null) {
                hostAddress = UNKNOWN_HOST;
            }
            return hostAddress;
        }else {
            return hostAddress;
        }
    }
    public static String getClientIp( MessageContext axis2MsgContext){
        String clientIp;
        Map headers =
                (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String xForwardForHeader = (String) headers.get(HEADER_X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(xForwardForHeader)){
            clientIp = xForwardForHeader;
            int idx = xForwardForHeader.indexOf(',');
            if (idx > -1) {
                clientIp = clientIp.substring(0, idx);
            }
        }else{
         clientIp = (String) axis2MsgContext.getProperty(MessageContext.REMOTE_ADDR);
        }
        return clientIp;
    }


    private static InetAddress getLocalAddress(){
        Enumeration<NetworkInterface> ifaces = null;
        try {
            ifaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("Failed to get host address", e);
        }
        if (ifaces != null) {
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isEnabledMetering() {
        return isEnabledMetering;
    }

    public static void setEnabledMetering(boolean enabledMetering) {
        isEnabledMetering = enabledMetering;
    }

    public static APIManagerAnalyticsConfiguration getApiManagerAnalyticsConfiguration() {
        return UsageComponent.getAmConfigService().getAPIAnalyticsConfiguration();
    }
}
