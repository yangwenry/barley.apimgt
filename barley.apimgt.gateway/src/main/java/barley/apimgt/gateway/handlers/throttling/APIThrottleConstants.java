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

package barley.apimgt.gateway.handlers.throttling;

import barley.apimgt.impl.APIConstants;

public class APIThrottleConstants {

    public static final int API_THROTTLE_OUT_ERROR_CODE = 900800;
    public static final int HARD_LIMIT_EXCEEDED_ERROR_CODE = 900801;
    public static final int RESOURCE_THROTTLE_OUT_ERROR_CODE = 900802;
    public static final int APPLICATION_THROTTLE_OUT_ERROR_CODE = 900803;
    public static final int SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE = 900804;
    public static final int SUBSCRIPTION_BURST_THROTTLE_OUT_ERROR_CODE = 900807;
    public static final int BLOCKED_ERROR_CODE = 900805;
    public static final int CUSTOM_POLICY_THROTTLE_OUT_ERROR_CODE = 900806;
    public static final long APPLICATION_THROTTLE_LIMIT = 500;

    public static final String API_LIMIT_EXCEEDED = APIConstants.THROTTLE_OUT_REASON_API_LIMIT_EXCEEDED;
    public static final String RESOURCE_LIMIT_EXCEEDED = APIConstants.THROTTLE_OUT_REASON_RESOURCE_LIMIT_EXCEEDED;
    public static final String APPLICATION_LIMIT_EXCEEDED = APIConstants.THROTTLE_OUT_REASON_APPLICATION_LIMIT_EXCEEDED;
    public static final String SUBSCRIPTION_LIMIT_EXCEEDED = APIConstants.THROTTLE_OUT_REASON_SUBSCRIPTION_LIMIT_EXCEEDED;
    public static final String CUSTOM_POLICY_LIMIT_EXCEED = "CUSTOM_POLICY_LIMIT_EXCEED";

    public static final String API_THROTTLE_NS = "http://codefarm.co.kr/apimanager/throttling";
    public static final String API_THROTTLE_NS_PREFIX = "amt";
    public static final String API_THROTTLE_OUT_HANDLER = "_throttle_out_handler_";
    public static final String HARD_THROTTLING_CONFIGURATION = "hard_throttling_limits";
    public static final String PRODUCTION_HARD_LIMIT = "PRODUCTION_HARD_LIMIT";
    public static final String SUBSCRIPTION_BURST_LIMIT = "SUBSCRIPTION_BURST_LIMIT";
    public static final String SANDBOX_HARD_LIMIT = "SANDBOX_HARD_LIMIT";
    public static final String THROTTLED_OUT_REASON = APIConstants.THROTTLE_OUT_REASON_KEY;
    public static final String THROTTLED_NEXT_ACCESS_TIMESTAMP = "NEXT_ACCESS_TIME";
    public static final String THROTTLED_NEXT_ACCESS_TIME = "NEXT_ACCESS_UTC_TIME";
    public static final String HARD_LIMIT_EXCEEDED = APIConstants.THROTTLE_OUT_REASON_HARD_LIMIT_EXCEEDED;
    public static final String SUBSCRIPTON_BURST_LIMIT_EXCEEDED = "SUBSCRIPTION_BURST_LIMIT_EXCEED";
    public static final String REQUEST_BLOCKED = "REQUEST_BLOCKED";
    public static final int SC_TOO_MANY_REQUESTS = 429;
    public static final String BLOCKED_REASON = APIConstants.BLOCKED_REASON_KEY;
    public static final String UTC = "UTC";
    public static final String IS_THROTTLED = "isThrottled";
    public static final String THROTTLE_KEY = "throttleKey";
    public static final String EXPIRY_TIMESTAMP = "expiryTimeStamp";
    public static final String IP = "ip";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String MESSAGE_SIZE = "messageSize";
    public static final String MIN = "min";
    public static final String WS_THROTTLE_POLICY_HEADER = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" " +
            "xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "    <throttle:MediatorThrottleAssertion>\n";
    public static final String WS_THROTTLE_POLICY_BOTTOM = "</throttle:MediatorThrottleAssertion>\n" +"</wsp:Policy>";
}
