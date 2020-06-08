package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.vo.UserSearchParam;

import java.util.List;

/**
 * Data access object interface to work with User entity database operations.
 *
 * @author Arthur Rukshan
 */
public interface UserPaymentDao {

    UserPayment loadUserPaymentByID(int paymentNo) throws UsageBillingException;

    List<UserPayment> loadUserPayments() throws UsageBillingException;

    List<UserPayment> loadUserPayments(String userId, int tenantId) throws UsageBillingException;

    List<UserPayment> loadUserPayments(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException;

    int countUserPayment(UserSearchParam userSearchParam) throws UsageBillingException;

    void addUserPayment(UserPayment userPayment) throws UsageBillingException;

    void updateUserPayment(UserPayment userPayment) throws UsageBillingException;

    void deleteUserPayment(int paymentNo) throws UsageBillingException;
}
