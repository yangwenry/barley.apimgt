/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package barley.apimgt.gateway.handlers.throttling;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis2.context.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import barley.apimgt.api.dto.ConditionDTO;
import barley.apimgt.api.dto.ConditionGroupDTO;
import barley.apimgt.api.model.policy.PolicyConstants;
import barley.apimgt.gateway.handlers.security.AuthenticationContext;
import barley.apimgt.gateway.utils.GatewayUtils;
import barley.apimgt.impl.APIConstants;
import barley.apimgt.impl.utils.APIUtil;

/**
 * This class is used by {@code ThrottleHandler} to determine Applicability of Throttling Conditions.
 * This is only
 * used in the flows where Advanced Throttling policies are used. When defining an Advanced Policy the capability has
 * been provided to add several condition groups. Using a condition group you can enable a special quota upon a
 * specific condition is met. For example you can specify something like, allow 50 req/min if User-Agent header is
 * Mozilla. Decision Engine keeps a track of which attributes are present in the request and which keys have been
 * throttled out. In order to see if those keys are applicable for the request, GW too should run some checks by
 * going through the attributes used for those conditions. What this class does is performing those checks.
 *
 */
public class ThrottleConditionEvaluator {

    private ThrottleConditionEvaluator() {

    }

    private static class ThrottleEvaluatorHolder {
        private static ThrottleConditionEvaluator evaluator = new ThrottleConditionEvaluator();
    }

    public static ThrottleConditionEvaluator getInstance() {
        return ThrottleEvaluatorHolder.evaluator;
    }

    /**
     * When called, provides a list of Applicable Condition Groups for the current request.
     * @param synapseContext Message Context of the incoming request.
     * @param authenticationContext AuthenticationContext populated by {@code APIAuthenticationHandler}
     * @param inputConditionGroups All Condition Groups Attached with the resource/API being invoked.
     * @return List of ConditionGroups applicable for the current request.
     */
    public List<ConditionGroupDTO> getApplicableConditions(org.apache.synapse.MessageContext synapseContext,
                                                           AuthenticationContext authenticationContext,
                                                           ConditionGroupDTO[] inputConditionGroups) {

        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>(inputConditionGroups.length);
        ConditionGroupDTO defaultGroup = null;

        for (ConditionGroupDTO conditionGroup : inputConditionGroups) {
            if (APIConstants.THROTTLE_POLICY_DEFAULT.equals(conditionGroup.getConditionGroupId())) {
                defaultGroup = conditionGroup;
            } else if (isConditionGroupApplicable(synapseContext, authenticationContext, conditionGroup)) {
                matchingConditions.add(conditionGroup);
            }
        }

        // If no matching ConditionGroups are present, apply the default group.
        if (matchingConditions.isEmpty()) {
            matchingConditions.add(defaultGroup);
        }

        return matchingConditions;
    }

    private boolean isConditionGroupApplicable(org.apache.synapse.MessageContext synapseContext,
                                               AuthenticationContext authenticationContext,
                                               ConditionGroupDTO conditionGroup) {
        ConditionDTO[] conditions = conditionGroup.getConditions();

        boolean evaluationState = true;

        if (conditions.length == 0) {
            evaluationState = false;
        }


        // When multiple conditions have been specified, all the conditions should occur.
        for (ConditionDTO condition : conditions) {
            evaluationState = evaluationState & isConditionApplicable(synapseContext, authenticationContext, condition);

            // If one of the conditions are false, rest will evaluate to false. So no need to check the rest.
            if(!evaluationState){
                return false;
            }
        }
        return evaluationState;
    }

    private boolean isConditionApplicable(org.apache.synapse.MessageContext synapseContext,
                                          AuthenticationContext authenticationContext,
                                          ConditionDTO condition) {


        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synapseContext).getAxis2MessageContext();

        boolean state = false;
        switch (condition.getConditionType()) {
            case PolicyConstants.IP_RANGE_TYPE: {
                state = isWithinIP(axis2MessageContext, condition);
                break;
            }
            case PolicyConstants.IP_SPECIFIC_TYPE: {
                state = isMatchingIP(axis2MessageContext, condition);
                break;
            }
            case PolicyConstants.QUERY_PARAMETER_TYPE: {
                state = isQueryParamPresent(axis2MessageContext, condition);
                break;
            }
            case PolicyConstants.JWT_CLAIMS_TYPE: {
                state = isJWTClaimPresent(authenticationContext, condition);
                break;
            }
            case PolicyConstants.HEADER_TYPE:{
                state = isHeaderPresent(axis2MessageContext,condition);
                break;
            }
        }

        if (condition.isInverted()) {
            state = !state;
        }

        return state;
    }

    private boolean isHeaderPresent(MessageContext messageContext, ConditionDTO condition) {
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) messageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if(transportHeaderMap != null){
            String value = transportHeaderMap.get(condition.getConditionName());
            if(value == null){
                return false;
            }
            Pattern pattern = Pattern.compile(condition.getConditionValue());
            Matcher matcher = pattern.matcher(value);
            return matcher.find();
        }
        return false;
    }

    private boolean isJWTClaimPresent(AuthenticationContext authenticationContext, ConditionDTO condition) {
        Map assertions = GatewayUtils.getJWTClaims(authenticationContext);

        Object value = assertions.get(condition.getConditionName());
        if (value == null) {
            return false;
        } else if(value instanceof String) {
            String valueString = (String) value;
            return valueString.matches(condition.getConditionValue());
        } else {
            return false;
        }
    }

    private boolean isQueryParamPresent(MessageContext messageContext, ConditionDTO condition) {

        Map<String, String> queryParamMap = GatewayUtils.getQueryParams(messageContext);

        String value = queryParamMap.get(condition.getConditionName());

        if (value == null) {
            return false;
        }
        return value.equals(condition.getConditionValue());
    }

    private boolean isMatchingIP(MessageContext messageContext, ConditionDTO condition) {
        String currentIpString = GatewayUtils.getIp(messageContext);
        return currentIpString.equals(condition.getConditionValue());
    }

    private boolean isWithinIP(MessageContext messageContext, ConditionDTO condition) {
        // For an IP Range Condition, starting IP is set as a the name, ending IP as the value.
        long startIp = APIUtil.ipToLong(condition.getConditionName());
        long endIp = APIUtil.ipToLong(condition.getConditionValue());

        String currentIpString = GatewayUtils.getIp(messageContext);
        if (!currentIpString.isEmpty()) {
            long currentIp = APIUtil.ipToLong(currentIpString);

            return startIp <= currentIp && endIp >= currentIp;
        }
        return false;
    }

}
