package barley.apimgt.usage.billing.services;

import barley.apimgt.usage.billing.domain.Plan;

import java.util.Map;

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
    boolean createPlan(Plan plan);

    /**
     * Create Usage plan - persist usage plan to database
     *
     * @param plan
     * @return true if success
     */
    boolean createUsagePlan(Plan plan);

    /**
     * Retrieves full User record from database by plan name
     *
     * @param planName
     * @return PlanEntity
     */
    Plan loadPlanEntityByPlanName(String planName);

    Map<String, Object> loadPlanEntities();

    Map<String, Object> loadPlanEntitiesString();

}
