package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.domain.Plan;
import barley.apimgt.usage.billing.exception.BillingException;

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
    Plan loadPlanByPlanName(String planName) throws BillingException;

    List<Plan> loadPlans() throws BillingException ;

    void addPlan(Plan plan) throws BillingException ;

    void updatePlan(Plan plan) throws BillingException ;

    void deletePlan(int planNo) throws BillingException ;
}
