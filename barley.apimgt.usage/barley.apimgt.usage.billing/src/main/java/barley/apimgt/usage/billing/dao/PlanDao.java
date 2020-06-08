package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.domain.Plan;
import barley.apimgt.usage.billing.exception.UsageBillingException;

import java.util.List;

/**
 * Data access object interface to work with User entity database operations.
 *
 * @author Arthur Rukshan
 */
public interface PlanDao {

    /**
     * Queries user by username
     *
     * @param planName
     * @return User entity
     */
    Plan loadPlanByPlanName(String planName) throws UsageBillingException;

    List<Plan> loadPlans() throws UsageBillingException;

    List<Plan> loadPlans(int page, int count, String planName) throws UsageBillingException;

    int countPlan(String planName) throws UsageBillingException;

    void addPlan(Plan plan) throws UsageBillingException;

    void updatePlan(Plan plan) throws UsageBillingException;

    void deletePlan(int planNo) throws UsageBillingException;
}
