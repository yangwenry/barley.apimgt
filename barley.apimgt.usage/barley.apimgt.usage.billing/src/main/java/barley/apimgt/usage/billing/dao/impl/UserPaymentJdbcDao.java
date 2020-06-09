package barley.apimgt.usage.billing.dao.impl;

import barley.apimgt.usage.billing.BillingDBUtil;
import barley.apimgt.usage.billing.dao.UserPaymentDao;
import barley.apimgt.usage.billing.domain.UserPayment;
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

public class UserPaymentJdbcDao implements UserPaymentDao {
    private static final Log log = LogFactory.getLog(UserPaymentJdbcDao.class);

    private UserPaymentJdbcDao() {}

    private static class UserPaymentJdbcDaoHolder {
        private static final UserPaymentJdbcDao INSTANCE = new UserPaymentJdbcDao();
    }

    public static UserPaymentJdbcDao getInstance() {
        return UserPaymentJdbcDaoHolder.INSTANCE;
    }

    private static final String FIELD_AM_BILLING_USER_PAYMENT_SQL =
              "SELECT PAYMENT_NO, USER_ID, TENANT_ID, USER_NAME, FIRST_NAME, LAST_NAME, USER_EMAIL, " +
                    " USER_PASSWORD, COUNTRY, CC_NUMBER, CVC, CARD_TYPE, CARD_EXP_DATE, ADDRESS1, ADDRESS2, ADDRESS3, " +
                    " CITY, PHONE_NUMBER, COMPANY ";

    @Override
    public UserPayment loadUserPaymentByID(int paymentNo) throws UsageBillingException {
        UserPayment userPayment = null;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_USER_PAYMENT_SQL +
                "  FROM AM_BILLING_USER_PAYMENT " +
                " WHERE PAYMENT_NO = ? ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, paymentNo);
            rs = ps.executeQuery();

            if (rs.next()) {
                userPayment = createUserPayment(rs);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get user payment by paymentNo: " + paymentNo;
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }
        return userPayment;
    }

    @Override
    public List<UserPayment> loadUserPayments() throws UsageBillingException {
        List<UserPayment> userPayments = new ArrayList<UserPayment>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_USER_PAYMENT_SQL +
                "  FROM AM_BILLING_USER_PAYMENT " +
                " ORDER BY PAYMENT_NO DESC ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();

            while (rs.next()) {
                UserPayment userPayment = createUserPayment(rs);
                userPayments.add(userPayment);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get user payments ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return userPayments;
    }

    @Override
    public List<UserPayment> loadUserPayments(String userId, int tenantId) throws UsageBillingException {
        List<UserPayment> userPayments = new ArrayList<UserPayment>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = FIELD_AM_BILLING_USER_PAYMENT_SQL +
                "  FROM AM_BILLING_USER_PAYMENT " +
                " WHERE USER_ID = ? " +
                "   AND TENANT_ID = ? " +
                " ORDER BY PAYMENT_NO ";
        try {
            conn = BillingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            int index = 1;
            ps.setString(index++, userId);
            ps.setInt(index++, tenantId);
            rs = ps.executeQuery();

            while (rs.next()) {
                UserPayment userPayment = createUserPayment(rs);
                userPayments.add(userPayment);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get user payments ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return userPayments;
    }

    @Override
    public List<UserPayment> loadUserPayments(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException {
        List<UserPayment> userPayments = new ArrayList<UserPayment>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int startNo = (page-1) * count;

        String sqlQuery = FIELD_AM_BILLING_USER_PAYMENT_SQL +
                "  FROM AM_BILLING_USER_PAYMENT " +
                " WHERE TENANT_ID = ? " +
                "   AND USER_ID LIKE ? " +
                "   AND USER_NAME LIKE ? " +
                " ORDER BY PAYMENT_NO DESC " +
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
                UserPayment userPayment = createUserPayment(rs);
                userPayments.add(userPayment);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while get user payments ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return userPayments;
    }

    @Override
    public int countUserPayment(UserSearchParam userSearchParam) throws UsageBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int total = 0;

        String sqlQuery = "SELECT COUNT(*) AS CNT " +
                "  FROM AM_BILLING_USER_PAYMENT " +
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
            String msg = "Error occurred while get count user payments ";
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return total;
    }

    private UserPayment createUserPayment(ResultSet rs) throws SQLException {
        UserPayment userPayment = new UserPayment();
        int paymentNo = rs.getInt("PAYMENT_NO");
        String userId = rs.getString("USER_ID");
        int tenantId = rs.getInt("TENANT_ID");
        String userName = rs.getString("USER_NAME");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        String userEmail = rs.getString("USER_EMAIL");
        String userPassword = rs.getString("USER_PASSWORD");
        String country = rs.getString("COUNTRY");
        String ccNumber = rs.getString("CC_NUMBER");
        String cvc = rs.getString("CVC");
        String cardType = rs.getString("CARD_TYPE");
        String cardExpDate = rs.getString("CARD_EXP_DATE");
        String address1 = rs.getString("ADDRESS1");
        String address2 = rs.getString("ADDRESS2");
        String address3 = rs.getString("ADDRESS3");
        String city = rs.getString("CITY");
        String phoneNumber = rs.getString("PHONE_NUMBER");
        String company = rs.getString("COMPANY");

        userPayment.setPaymentNo(paymentNo);
        userPayment.setUserId(userId);
        userPayment.setTenantId(tenantId);
        userPayment.setUserName(userName);
        userPayment.setFirstName(firstName);
        userPayment.setLastName(lastName);
        userPayment.setUserEmail(userEmail);
        userPayment.setPassword(userPassword);
        userPayment.setCountry(country);
        userPayment.setCcNumber(ccNumber);
        userPayment.setCvc(cvc);
        userPayment.setCardType(cardType);
        userPayment.setCardExpDate(cardExpDate);
        userPayment.setAddress1(address1);
        userPayment.setAddress2(address2);
        userPayment.setAddress3(address3);
        userPayment.setCity(city);
        userPayment.setPhoneNumber(phoneNumber);
        userPayment.setCompany(company);
        return userPayment;
    }

    @Override
    public void addUserPayment(UserPayment userPayment) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "INSERT INTO AM_BILLING_USER_PAYMENT(USER_ID, TENANT_ID, USER_NAME, FIRST_NAME, LAST_NAME, " +
                    " USER_EMAIL, USER_PASSWORD, COUNTRY, CC_NUMBER, CVC, CARD_TYPE, CARD_EXP_DATE, ADDRESS1, ADDRESS2, ADDRESS3, " +
                    " CITY, PHONE_NUMBER, COMPANY) " +
                    " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

            ps = conn.prepareStatement(sqlAddQuery);
            int index = 1;
            ps.setString(index++, userPayment.getUserId());
            ps.setInt(index++, userPayment.getTenantId());
            ps.setString(index++, userPayment.getUserName());
            ps.setString(index++, userPayment.getFirstName());
            ps.setString(index++, userPayment.getLastName());
            ps.setString(index++, userPayment.getUserEmail());
            ps.setString(index++, userPayment.getPassword());
            ps.setString(index++, userPayment.getCountry());
            ps.setString(index++, userPayment.getCcNumber());
            ps.setString(index++, userPayment.getCvc());
            ps.setString(index++, userPayment.getCardType());
            ps.setString(index++, userPayment.getCardExpDate());
            ps.setString(index++, userPayment.getAddress1());
            ps.setString(index++, userPayment.getAddress2());
            ps.setString(index++, userPayment.getAddress3());
            ps.setString(index++, userPayment.getCity());
            ps.setString(index++, userPayment.getPhoneNumber());
            ps.setString(index++, userPayment.getCompany());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to add user payment of the user : " + userPayment.getUserName();
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void updateUserPayment(UserPayment userPayment) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "UPDATE AM_BILLING_USER_PAYMENT SET USER_NAME = ?, FIRST_NAME = ?, LAST_NAME = ?, " +
                    " USER_EMAIL = ?, COUNTRY = ?, CC_NUMBER = ?, CVC = ?, CARD_TYPE = ?, CARD_EXP_DATE = ?, ADDRESS1 = ?, ADDRESS2 = ?, ADDRESS3 = ?, " +
                    " CITY = ?, PHONE_NUMBER = ?, COMPANY = ? " +
                    " WHERE PAYMENT_NO = ? ";

            ps = conn.prepareStatement(sqlAddQuery);
            int index = 1;
            //ps.setString(index++, userPayment.getUserId());
            //ps.setInt(index++, userPayment.getTenantId());
            ps.setString(index++, userPayment.getUserName());
            ps.setString(index++, userPayment.getFirstName());
            ps.setString(index++, userPayment.getLastName());
            ps.setString(index++, userPayment.getUserEmail());
            //ps.setString(index++, userPayment.getPassword());
            ps.setString(index++, userPayment.getCountry());
            ps.setString(index++, userPayment.getCcNumber());
            ps.setString(index++, userPayment.getCvc());
            ps.setString(index++, userPayment.getCardType());
            ps.setString(index++, userPayment.getCardExpDate());
            ps.setString(index++, userPayment.getAddress1());
            ps.setString(index++, userPayment.getAddress2());
            ps.setString(index++, userPayment.getAddress3());
            ps.setString(index++, userPayment.getCity());
            ps.setString(index++, userPayment.getPhoneNumber());
            ps.setString(index++, userPayment.getCompany());
            ps.setInt(index++, userPayment.getPaymentNo());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to update user payment of the user : " + userPayment.getUserName();
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void deleteUserPayment(int paymentNo) throws UsageBillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "DELETE FROM AM_BILLING_USER_PAYMENT WHERE PAYMENT_NO = ? ";

            ps = conn.prepareStatement(sqlAddQuery);
            int index = 1;
            ps.setInt(index++, paymentNo);
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to delete user payment of the paymentNo : " + paymentNo;
            log.error(msg, e);
            throw new UsageBillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }
}
