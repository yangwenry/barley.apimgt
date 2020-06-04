package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.BillingDBUtil;
import barley.apimgt.usage.billing.domain.Plan;
import barley.apimgt.usage.billing.exception.BillingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlanJdbcDao implements PlanDao {

    private static final Log log = LogFactory.getLog(PlanJdbcDao.class);

    private PlanJdbcDao() {}

    private static class PlanJdbcDaoHolder {
        private static final PlanJdbcDao INSTANCE = new PlanJdbcDao();
    }

    public static PlanJdbcDao getInstance() {
        return PlanJdbcDaoHolder.INSTANCE;
    }

    @Override
    public Plan loadPlanByPlanName(String planName) throws BillingException {
        Plan plan = null;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = "SELECT PLAN_NO, PLAN_NAME, PLAN_TYPE, QUOTA, FEE_PER_REQUEST, SUBSCRIPTION_FEE, FEE_RATE " +
                          "  FROM AM_BILLING_PLAN " +
                          " WHERE PLAN_NAME = ? ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, planName);
            rs = ps.executeQuery();

            if (rs.next()) {
                plan = createPlan(rs);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get billing plan by planName: " + planName;
            log.error(msg, e);
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }
        return plan;
    }

    @Override
    public List<Plan> loadPlans() throws BillingException {

        List<Plan> plans = new ArrayList<Plan>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = "SELECT PLAN_NO, PLAN_NAME, PLAN_TYPE, QUOTA, FEE_PER_REQUEST, SUBSCRIPTION_FEE, FEE_RATE " +
                "  FROM AM_BILLING_PLAN" +
                " ORDER BY PLAN_NO DESC ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();

            while (rs.next()) {
                Plan plan = createPlan(rs);
                plans.add(plan);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get billing plans ";
            log.error(msg, e);
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return plans;
    }

    private Plan createPlan(ResultSet rs) throws SQLException {
        Plan plan = new Plan();
        int planNo = rs.getInt("PLAN_NO");
        String getPlanName = rs.getString("PLAN_NAME");
        String quota = rs.getString("QUOTA");
        String planType = rs.getString("PLAN_TYPE");
        double feePerRequest = rs.getDouble("FEE_PER_REQUEST");
        double subscriptionFee = rs.getDouble("SUBSCRIPTION_FEE");
        double feeRate = rs.getDouble("FEE_RATE");
        plan.setPlanNo(planNo);
        plan.setPlanName(getPlanName);
        plan.setPlanType(planType);
        plan.setQuota(quota);
        plan.setFeePerRequest(feePerRequest);
        plan.setSubscriptionFee(subscriptionFee);
        plan.setFeeRate(feeRate);
        return plan;
    }

    @Override
    public void addPlan(Plan plan) throws BillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "INSERT INTO AM_BILLING_PLAN(PLAN_NAME, PLAN_TYPE, QUOTA, FEE_PER_REQUEST, SUBSCRIPTION_FEE, FEE_RATE) VALUES(?, ?, ?, ?, ?, ?)";

            ps = conn.prepareStatement(sqlAddQuery);
            ps.setString(1, plan.getPlanName());
            ps.setString(2, plan.getPlanType());
            ps.setString(3, plan.getQuota());
            ps.setDouble(4, plan.getFeePerRequest());
            ps.setDouble(5, plan.getSubscriptionFee());
            ps.setDouble(6, plan.getFeeRate());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to add billing plan of the plan Name: " + plan.getPlanName();
            log.error(msg, e);
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void updatePlan(Plan plan) throws BillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "UPDATE AM_BILLING_PLAN SET PLAN_NAME = ?, PLAN_TYPE = ?, QUOTA = ?, FEE_PER_REQUEST = ?, " +
                    "SUBSCRIPTION_FEE = ?, FEE_RATE = ? " +
                    "WHERE PLAN_NO = ? ";

            ps = conn.prepareStatement(sqlAddQuery);
            ps.setString(1, plan.getPlanName());
            ps.setString(2, plan.getPlanType());
            ps.setString(3, plan.getQuota());
            ps.setDouble(4, plan.getFeePerRequest());
            ps.setDouble(5, plan.getSubscriptionFee());
            ps.setDouble(6, plan.getFeeRate());
            ps.setInt(7, plan.getPlanNo());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to update billing plan of the plan Name: " + plan.getPlanName();
            log.error(msg, e);
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void deletePlan(int planNo) throws BillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "DELETE FROM AM_BILLING_PLAN WHERE PLAN_NO = ?";

            ps = conn.prepareStatement(sqlAddQuery);
            ps.setInt(1, planNo);
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to delete billing plan of the plan no: " + planNo;
            log.error(msg, e);
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

}
