package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.BillingDBUtil;
import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.BillingException;
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

    @Override
    public UserPayment loadUserPaymentByID(int paymentNo) throws BillingException {
        UserPayment userPayment = null;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = "SELECT PAYMENT_NO, USER_ID, TENANT_ID, USER_NAME, FIRST_NAME, LAST_NAME, USER_EMAIL, " +
                " USER_PASSWORD, COUNTRY, CC_NUMBER, CVC, CARD_TYPE, CARD_EXP_DATE, ADDRESS1, ADDRESS2, ADDRESS3, " +
                " CITY, PHONE_NUMBER, COMPANY " +
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
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }
        return userPayment;
    }

    @Override
    public List<UserPayment> loadUserPayments() throws BillingException {
        List<UserPayment> userPayments = new ArrayList<UserPayment>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = "SELECT PAYMENT_NO, USER_ID, TENANT_ID, USER_NAME, FIRST_NAME, LAST_NAME, USER_EMAIL, " +
                " USER_PASSWORD, COUNTRY, CC_NUMBER, CVC, CARD_TYPE, CARD_EXP_DATE, ADDRESS1, ADDRESS2, ADDRESS3, " +
                " CITY, PHONE_NUMBER, COMPANY " +
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
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, rs);
        }

        return userPayments;
    }

    private UserPayment createUserPayment(ResultSet rs) throws SQLException {
        UserPayment userPayment = new UserPayment();
        int paymentNo = rs.getInt("");
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
    public void addUserPayment(UserPayment userPayment) throws BillingException {
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
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void updateUserPayment(UserPayment userPayment) throws BillingException {
        PreparedStatement ps = null;
        Connection conn = null;

        try {
            conn = BillingDBUtil.getConnection();

            String sqlAddQuery = "UPDATE AM_BILLING_USER_PAYMENT SET USER_NAME = ?, FIRST_NAME = ?, LAST_NAME = ?, " +
                    " USER_EMAIL = ?, USER_PASSWORD = ?, COUNTRY = ?, CC_NUMBER = ?, CVC = ?, CARD_TYPE = ?, CARD_EXP_DATE = ?, ADDRESS1 = ?, ADDRESS2 = ?, ADDRESS3 = ?, " +
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
            ps.setString(index++, userPayment.getPassword());
            ps.setString(index++, userPayment.getCountry());
            ps.setString(index++, userPayment.getCcNumber());
            ps.setString(index++, userPayment.getCvc());
            ps.setString(index++, userPayment.getCardType());
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
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public void deleteUserPayment(int paymentNo) throws BillingException {
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
            throw new BillingException(msg, e);
        } finally {
            BillingDBUtil.closeAllConnections(ps, conn, null);
        }
    }
}
