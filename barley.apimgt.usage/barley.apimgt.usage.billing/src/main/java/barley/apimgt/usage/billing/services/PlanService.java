package barley.apimgt.usage.billing.services;

import barley.apimgt.usage.billing.domain.Plan;
import barley.apimgt.usage.billing.exception.UsageBillingException;

import java.util.List;

/**
 * Service providing service methods to work with user data and entity.
 *
 * @author Arthur Vin
 */
public interface PlanService {

    /**
     * Create plan - persist to database
     *
     * @param plan
     * @return true if success
     */
    void createPlan(Plan plan) throws UsageBillingException;

    /**
     * Create Usage plan - persist usage plan to database
     *
     * @param plan
     * @return true if success
     */
    void createUsagePlan(Plan plan) throws UsageBillingException;

    /**
     * Retrieves full User record from database by plan name
     *
     * @param planName
     * @return PlanEntity
     */
    Plan loadPlanByPlanName(String planName) throws UsageBillingException;

    List<Plan> listPlans() throws UsageBillingException;

    List<Plan> listPlans(int page, int count, String planName) throws UsageBillingException;

    int countPlan(String planName) throws UsageBillingException;

    void updatePlan(Plan plan) throws UsageBillingException;

    void deletePlan(int planNo) throws UsageBillingException;


}
