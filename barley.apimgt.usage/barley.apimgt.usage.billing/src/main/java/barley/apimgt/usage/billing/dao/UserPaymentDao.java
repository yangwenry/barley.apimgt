package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.BillingException;

import java.util.List;

/**
 * Data access object interface to work with User entity database operations.
 *
 * @author Arthur Rukshan
 */
public interface UserPaymentDao {

    UserPayment loadUserPaymentByID(int paymentNo) throws BillingException;

    List<UserPayment> loadUserPayments() throws BillingException ;

    void addUserPayment(UserPayment userPayment) throws BillingException ;

    void updateUserPayment(UserPayment userPayment) throws BillingException ;

    void deleteUserPayment(int paymentNo) throws BillingException ;
}
