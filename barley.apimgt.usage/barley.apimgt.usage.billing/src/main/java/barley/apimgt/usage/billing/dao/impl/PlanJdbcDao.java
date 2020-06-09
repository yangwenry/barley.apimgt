package barley.apimgt.usage.billing.dao.impl;

import barley.apimgt.usage.billing.BillingDBUtil;
import barley.apimgt.usage.billing.dao.PlanDao;
import barley.apimgt.usage.billing.domain.Plan;
import barley.apimgt.usage.billing.exception.UsageBillingException;
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

    private static final String FIELD_AM_BILLING_PLAN_SQL = "SELECT PLAN_NO, PLAN_NAME, PLAN_TYPE, QUOTA, FEE_PER_REQUEST, SUBSCRIPTION_FEE, FEE_RATE ";

    @Override
    public Plan loadPlanByPlanName(String planName) throws UsageBillingException {
        Plan plan = null;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_PLAN_SQL +
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
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }
        return plan;
    }

    @Override
    public List<Plan> loadPlans() throws UsageBillingException {

        List<Plan> plans = new ArrayList<Plan>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_PLAN_SQL +
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
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return plans;
    }

    @Override
    public List<Plan> loadPlans(int page, int count, String planName) throws UsageBillingException {
        List<Plan> plans = new ArrayList<Plan>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int startNo = (page-1) * count;

        String sqlQuery = FIELD_AM_BILLING_PLAN_SQL +
                "  FROM AM_BILLING_PLAN" +
                " WHERE PLAN_NAME LIKE ? " +
                " ORDER BY PLAN_NO DESC " +
                " LIMIT ?, ? ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            int index = 1;
            if(planName == null) planName = "";
            ps.setString(index++, "%" + planName + "%");
            ps.setInt(index++, startNo);
            ps.setInt(index++, count);

            rs = ps.executeQuery();
            while (rs.next()) {
                Plan plan = createPlan(rs);
                plans.add(plan);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get billing plans ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return plans;
    }

    @Override
    public int countPlan(String planName) throws UsageBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int total = 0;

        String sqlQuery = "SELECT COUNT(*) AS CNT " +
                "  FROM AM_BILLING_PLAN" +
                " WHERE PLAN_NAME LIKE ? ";

        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            int index = 1;
            if(planName == null) planName = "";
            ps.setString(index++, "%" + planName + "%");

            rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt("CNT");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get count billing plans ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return total;
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
    public void addPlan(Plan plan) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "INSERT INTO AM_BILLING_PLAN(PLAN_NAME, PLAN_TYPE, QUOTA, FEE_PER_REQUEST, SUBSCRIPTION_FEE, FEE_RATE) VALUES(?, ?, ?, ?, ?, ?)";

            ps = conn.prepareStatement(sqlAddQuery);
            int index = 1;
            ps.setString(index++, plan.getPlanName());
            ps.setString(index++, plan.getPlanType());
            ps.setString(index++, plan.getQuota());
            ps.setDouble(index++, plan.getFeePerRequest());
            ps.setDouble(index++, plan.getSubscriptionFee());
            ps.setDouble(index++, plan.getFeeRate());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to add billing plan of the plan Name: " + plan.getPlanName();
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void updatePlan(Plan plan) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "UPDATE AM_BILLING_PLAN SET PLAN_TYPE = ?, QUOTA = ?, FEE_PER_REQUEST = ?, " +
                    "SUBSCRIPTION_FEE = ?, FEE_RATE = ? " +
                    "WHERE PLAN_NO = ? ";

            ps = conn.prepareStatement(sqlAddQuery);
            int index = 1;
            //ps.setString(1, plan.getPlanName());
            ps.setString(index++, plan.getPlanType());
            ps.setString(index++, plan.getQuota());
            ps.setDouble(index++, plan.getFeePerRequest());
            ps.setDouble(index++, plan.getSubscriptionFee());
            ps.setDouble(index++, plan.getFeeRate());
            ps.setInt(index++, plan.getPlanNo());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to update billing plan of the plan Name: " + plan.getPlanName();
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void deletePlan(int planNo) throws UsageBillingException {
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
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

}
