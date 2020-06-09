package barley.apimgt.usage.billing.dao.impl;

import barley.apimgt.usage.billing.BillingDBUtil;
import barley.apimgt.usage.billing.dao.InvoiceDao;
import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.vo.UserSearchParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InvoiceJdbcDao implements InvoiceDao {
    private static final Log log = LogFactory.getLog(InvoiceJdbcDao.class);

    private InvoiceJdbcDao() {}

    private static class InvoiceJdbcDaoHolder {
        private static final InvoiceJdbcDao INSTANCE = new InvoiceJdbcDao();
    }

    public static InvoiceJdbcDao getInstance() {
        return InvoiceJdbcDaoHolder.INSTANCE;
    }

    private static final String FIELD_AM_BILLING_INVOICE_SQL =
            "SELECT INVOICE_NO, INVOICE_YEAR, INVOICE_MONTH, USER_ID, TENANT_ID, USER_NAME, FIRST_NAME, LAST_NAME, " +
                    " USER_COMPANY, USER_EMAIL, ADDRESS1, ADDRESS2, ADDRESS3, PAYMENT_METHOD, SUCCESS_COUNT, THROTTLE_COUNT, " +
                    " CREATED_DATE, SUBSCRIPTION_FEE, SUCCESS_FEE, THROTTLE_FEE, TOTAL_FEE, FEE_PER_SUCCESS, FEE_PER_THROTTLE, PLAN_NAME, PLAN_TYPE ";

    @Override
    public Invoice loadInvoiceByID(int invoiceNo) throws UsageBillingException {
        Invoice invoice = null;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_INVOICE_SQL +
                "  FROM AM_BILLING_INVOICE " +
                " WHERE INVOICE_NO = ? ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, invoiceNo);
            rs = ps.executeQuery();

            if (rs.next()) {
                invoice = createInvoice(rs);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get billing invoice by invoiceNo: " + invoiceNo;
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }
        return invoice;
    }

    @Override
    public List<Invoice> loadInvoices() throws UsageBillingException {
        List<Invoice> invoices = new ArrayList<Invoice>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_INVOICE_SQL +
                "  FROM AM_BILLING_INVOICE " +
                " ORDER BY INVOICE_NO DESC ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();

            while (rs.next()) {
                Invoice invoice = createInvoice(rs);
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get billing invoices ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return invoices;
    }

    @Override
    public List<Invoice> loadInvoices(int year, int month, int tenantId) throws UsageBillingException {
        List<Invoice> invoices = new ArrayList<Invoice>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_INVOICE_SQL +
                "  FROM AM_BILLING_INVOICE " +
                " WHERE INVOICE_YEAR = ? " +
                "   AND INVOICE_MONTH = ? " +
                "   AND TENANT_ID = ? " +
                " ORDER BY INVOICE_NO DESC ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            int index = 1;
            ps.setInt(index++, year);
            ps.setInt(index++, month);
            ps.setInt(index++, tenantId);

            rs = ps.executeQuery();
            while (rs.next()) {
                Invoice invoice = createInvoice(rs);
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get billing invoices ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return invoices;
    }

    @Override
    public List<Invoice> loadInvoices(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException {
        List<Invoice> invoices = new ArrayList<Invoice>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int startNo = (page-1) * count;

        String sqlQuery = FIELD_AM_BILLING_INVOICE_SQL +
                "  FROM AM_BILLING_INVOICE " +
                " WHERE TENANT_ID = ? " +
                "   AND USER_ID LIKE ? " +
                "   AND USER_NAME LIKE ? " +
                " ORDER BY INVOICE_NO DESC " +
                " LIMIT ?, ? ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            int index = 1;
            ps.setInt(index++, userSearchParam.getTenantId());
            if(userSearchParam.getUserId() == null) userSearchParam.setUserId("");
            ps.setString(index++, "%" + userSearchParam.getUserId() + "%");
            if(userSearchParam.getUserName() == null) userSearchParam.setUserName("");
            ps.setString(index++, "%" + userSearchParam.getUserName() + "%");
            ps.setInt(index++, startNo);
            ps.setInt(index++, count);

            rs = ps.executeQuery();
            while (rs.next()) {
                Invoice invoice = createInvoice(rs);
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get billing invoices ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return invoices;
    }

    @Override
    public int countInvoice(UserSearchParam userSearchParam) throws UsageBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int total = 0;

        String sqlQuery = "SELECT COUNT(*) AS CNT " +
                "  FROM AM_BILLING_INVOICE " +
                " WHERE TENANT_ID = ? " +
                "   AND USER_ID LIKE ? " +
                "   AND USER_NAME LIKE ? ";

        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            int index = 1;
            ps.setInt(index++, userSearchParam.getTenantId());
            if(userSearchParam.getUserId() == null) userSearchParam.setUserId("");
            ps.setString(index++, "%" + userSearchParam.getUserId() + "%");
            if(userSearchParam.getUserName() == null) userSearchParam.setUserName("");
            ps.setString(index++, "%" + userSearchParam.getUserName() + "%");

            rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt("CNT");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get count billing invoices ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return total;
    }

    private Invoice createInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        int invoiceNo = rs.getInt("INVOICE_NO");
        int invoiceYear = rs.getInt("INVOICE_YEAR");
        int invoiceMonth = rs.getInt("INVOICE_MONTH");
        String userId = rs.getString("USER_ID");
        int tenantId = rs.getInt("TENANT_ID");
        String userName = rs.getString("USER_NAME");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        String userCompany = rs.getString("USER_COMPANY");
        String userEmail = rs.getString("USER_EMAIL");
        String address1 = rs.getString("ADDRESS1");
        String address2 = rs.getString("ADDRESS2");
        String address3 = rs.getString("ADDRESS3");
        String paymentMethod = rs.getString("PAYMENT_METHOD");
        int successCount = rs.getInt("SUCCESS_COUNT");
        int throttleCount = rs.getInt("THROTTLE_COUNT");
        String createdDate = rs.getString("CREATED_DATE");
        double subscriptionFee = rs.getDouble("SUBSCRIPTION_FEE");
        double successFee = rs.getDouble("SUCCESS_FEE");
        double throttleFee = rs.getDouble("THROTTLE_FEE");
        double totalFee = rs.getDouble("TOTAL_FEE");
        double feePerSuccess = rs.getDouble("FEE_PER_SUCCESS");
        double feePerThrottle = rs.getDouble("FEE_PER_THROTTLE");
        String planName = rs.getString("PLAN_NAME");
        String planType = rs.getString("PLAN_TYPE");

        invoice.setInvoiceNo(invoiceNo);
        invoice.setInvoiceYear(invoiceYear);
        invoice.setInvoiceMonth(invoiceMonth);
        invoice.setUserId(userId);
        invoice.setUserName(userName);
        invoice.setFirstName(firstName);
        invoice.setLastName(lastName);
        invoice.setTenantId(tenantId);
        invoice.setUserCompany(userCompany);
        invoice.setUserEmail(userEmail);
        invoice.setAddress1(address1);
        invoice.setAddress2(address2);
        invoice.setAddress3(address3);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setSuccessCount(successCount);
        invoice.setThrottleCount(throttleCount);
        invoice.setCreatedDate(createdDate);
        invoice.setSubscriptionFee(subscriptionFee);
        invoice.setSuccessFee(successFee);
        invoice.setThrottleFee(throttleFee);
        invoice.setTotalFee(totalFee);
        invoice.setFeePerSuccess(feePerSuccess);
        invoice.setFeePerThrottle(feePerThrottle);
        invoice.setPlanName(planName);
        invoice.setPlanType(planType);

        return invoice;
    }

    @Override
    public void addInvoice(Invoice invoice) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "INSERT INTO AM_BILLING_INVOICE(INVOICE_YEAR, INVOICE_MONTH, USER_ID, TENANT_ID, USER_NAME, FIRST_NAME, LAST_NAME, " +
                    " USER_COMPANY, USER_EMAIL, ADDRESS1, ADDRESS2, ADDRESS3, PAYMENT_METHOD, SUCCESS_COUNT, THROTTLE_COUNT, " +
                    " CREATED_DATE, SUBSCRIPTION_FEE, SUCCESS_FEE, THROTTLE_FEE, TOTAL_FEE, FEE_PER_SUCCESS, FEE_PER_THROTTLE, PLAN_NAME, PLAN_TYPE) " +
                    " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

            ps = conn.prepareStatement(sqlAddQuery);
            int index = 1;
            ps.setInt(index++, invoice.getInvoiceYear());
            ps.setInt(index++, invoice.getInvoiceMonth());
            ps.setString(index++, invoice.getUserId());
            ps.setInt(index++, invoice.getTenantId());
            ps.setString(index++, invoice.getUserName());
            ps.setString(index++, invoice.getFirstName());
            ps.setString(index++, invoice.getLastName());
            ps.setString(index++, invoice.getUserCompany());
            ps.setString(index++, invoice.getUserEmail());
            ps.setString(index++, invoice.getAddress1());
            ps.setString(index++, invoice.getAddress2());
            ps.setString(index++, invoice.getAddress3());
            ps.setString(index++, invoice.getPaymentMethod());
            ps.setDouble(index++, invoice.getSuccessCount());
            ps.setDouble(index++, invoice.getThrottleCount());
            ps.setString(index++, invoice.getCreatedDate());
            ps.setDouble(index++, invoice.getSubscriptionFee());
            ps.setDouble(index++, invoice.getSuccessFee());
            ps.setDouble(index++, invoice.getThrottleFee());
            ps.setDouble(index++, invoice.getTotalFee());
            ps.setDouble(index++, invoice.getFeePerSuccess());
            ps.setDouble(index++, invoice.getFeePerThrottle());
            ps.setString(index++, invoice.getPlanName());
            ps.setString(index++, invoice.getPlanType());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to add billing invoice of the user : " + invoice.getUserName();
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void updateInvoice(Invoice invoice) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "UPDATE AM_BILLING_INVOICE SET USER_NAME = ?, FIRST_NAME = ?, LAST_NAME = ?, " +
                    " USER_COMPANY = ?, USER_EMAIL = ?, ADDRESS1 = ?, ADDRESS2 = ?, ADDRESS3 = ?, PAYMENT_METHOD = ?, SUCCESS_COUNT = ?, THROTTLE_COUNT = ?, " +
                    " SUBSCRIPTION_FEE = ?, SUCCESS_FEE = ?, THROTTLE_FEE = ?, TOTAL_FEE = ?, FEE_PER_SUCCESS = ?, FEE_PER_THROTTLE = ?, PLAN_NAME = ?, PLAN_TYPE = ? " +
                    " WHERE INVOICE_NO = ? ";

            ps = conn.prepareStatement(sqlAddQuery);
            int index = 1;
            //ps.setInt(index++, invoice.getInvoiceYear());
            //ps.setInt(index++, invoice.getInvoiceMonth());
            //ps.setString(index++, invoice.getUserId());
            //ps.setInt(index++, invoice.getTenantId());
            ps.setString(index++, invoice.getUserName());
            ps.setString(index++, invoice.getFirstName());
            ps.setString(index++, invoice.getLastName());
            ps.setString(index++, invoice.getUserCompany());
            ps.setString(index++, invoice.getUserEmail());
            ps.setString(index++, invoice.getAddress1());
            ps.setString(index++, invoice.getAddress2());
            ps.setString(index++, invoice.getAddress3());
            ps.setString(index++, invoice.getPaymentMethod());
            ps.setDouble(index++, invoice.getSuccessCount());
            ps.setDouble(index++, invoice.getThrottleCount());
            //ps.setString(index++, invoice.getCreatedDate());
            ps.setDouble(index++, invoice.getSubscriptionFee());
            ps.setDouble(index++, invoice.getSuccessFee());
            ps.setDouble(index++, invoice.getThrottleFee());
            ps.setDouble(index++, invoice.getTotalFee());
            ps.setDouble(index++, invoice.getFeePerSuccess());
            ps.setDouble(index++, invoice.getFeePerThrottle());
            ps.setString(index++, invoice.getPlanName());
            ps.setString(index++, invoice.getPlanType());
            ps.setInt(index++, invoice.getInvoiceNo());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to update billing invoice of the invoice no : " + invoice.getInvoiceNo();
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void deleteInvoice(int invoiceNo) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "DELETE FROM AM_BILLING_INVOICE WHERE INVOICE_NO = ? ";

            ps = conn.prepareStatement(sqlAddQuery);
            ps.setInt(1, invoiceNo);
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to delete billing invoice of the invoice no : " + invoiceNo;
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void deleteInvoice(int year, int month) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "DELETE FROM AM_BILLING_INVOICE WHERE INVOICE_YEAR = ? AND INVOICE_MONTH = ?";

            ps = conn.prepareStatement(sqlAddQuery);
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to delete billing invoice of the invoice year : " + year + ", month: " + month;
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

}
