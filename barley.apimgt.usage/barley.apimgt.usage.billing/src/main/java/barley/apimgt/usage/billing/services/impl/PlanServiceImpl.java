package barley.apimgt.usage.billing.services.impl;


import barley.apimgt.usage.billing.dao.PlanDao;
import barley.apimgt.usage.billing.domain.Plan;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.services.PlanService;

import java.util.List;

/**
 * Service providing service methods to work with user data and entity.
 *
 * @author Arthur Rukshan
 */
public class PlanServiceImpl implements PlanService {

    private PlanDao planDao;

    public PlanServiceImpl(PlanDao planDao) {
        this.planDao = planDao;
    }

    public PlanDao getPlanDao() {
        return planDao;
    }

    public void setPlanDao(PlanDao planDao) {
        this.planDao = planDao;
    }

    @Override
    public void createPlan(Plan plan) throws UsageBillingException {
        if(isExistPlan(plan.getPlanName())) {
            String msg = "already exists billing plans. ";
            throw new UsageBillingException(msg);
        }
        plan.setPlanType(Plan.PLAN_TYPES.STANDARD.toString());
        planDao.addPlan(plan);
    }

    @Override
    public void createUsagePlan(Plan plan) throws UsageBillingException {
        if(isExistPlan(plan.getPlanName())) {
            String msg = "already exists billing plans. ";
            throw new UsageBillingException(msg);
        }
        plan.setPlanType(Plan.PLAN_TYPES.USAGE.toString());
        planDao.addPlan(plan);
    }

    private boolean isExistPlan(String planName) throws UsageBillingException {
        return planDao.loadPlanByPlanName(planName) != null;
    }

    @Override
    public Plan loadPlanByPlanName(String planName) throws UsageBillingException {
        return planDao.loadPlanByPlanName(planName);
    }

    @Override
    public Plan loadPlanByPlanNo(int planNo) throws UsageBillingException {
        return planDao.loadPlanByPlanNo(planNo);
    }

    @Override
    public List<Plan> listPlans() throws UsageBillingException {
        return planDao.loadPlans();
    }

    @Override
    public List<Plan> listPlans(int page, int count, String planName) throws UsageBillingException {
        return planDao.loadPlans(page, count, planName);
    }

    @Override
    public int countPlan(String planName) throws UsageBillingException {
        return planDao.countPlan(planName);
    }

    @Override
    public void updatePlan(Plan plan) throws UsageBillingException {
        planDao.updatePlan(plan);
    }

    @Override
    public void deletePlan(int planNo) throws UsageBillingException {
        planDao.deletePlan(planNo);
    }


}
