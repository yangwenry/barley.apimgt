package barley.apimgt.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import barley.apimgt.api.dto.ConditionGroupDTO;
import barley.apimgt.impl.APIConstants;

public class VerbInfoDTO implements Serializable {

    private String httpVerb;

    private String authType;

    private String throttling;

    private String applicableLevel;

    private List<String> throttlingConditions = new ArrayList<String>();

    private String requestKey;

    private ConditionGroupDTO[] conditionGroups;

    public String getThrottling() {
        return throttling;
    }

    public void setThrottling(String throttling) {
        this.throttling = throttling;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public boolean requiresAuthentication() {
        return !APIConstants.AUTH_TYPE_NONE.equalsIgnoreCase(authType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerbInfoDTO that = (VerbInfoDTO) o;

        if (httpVerb != null ? !httpVerb.equals(that.getHttpVerb()) : that.getHttpVerb() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return httpVerb != null ? httpVerb.hashCode() : 0;
    }


    public List<String> getThrottlingConditions() {
        return throttlingConditions;
    }

    public void setThrottlingConditions(List<String> throttlingConditions) {
        this.throttlingConditions = throttlingConditions;
    }

    public String getApplicableLevel() {
        return applicableLevel;
    }

    public void setApplicableLevel(String applicableLevel) {
        this.applicableLevel = applicableLevel;
    }

    public void setConditionGroups(ConditionGroupDTO[] conditionGroups) {
        this.conditionGroups = conditionGroups;
    }

    public ConditionGroupDTO[] getConditionGroups() {
        return conditionGroups;
    }
}
