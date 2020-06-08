package barley.apimgt.usage.billing.services;

import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.vo.UserSearchParam;

import java.util.List;

/**
 * Service providing service methods to work with user data and entity.
 * 
 * @author Arthur Vin
 */
public interface UserPaymentService {

	void createUserPayment(UserPayment userPayment) throws UsageBillingException;

	List<UserPayment> listUserPayments() throws UsageBillingException;

	List<UserPayment> listUserPayments(String userId, int tenantId) throws UsageBillingException;

	List<UserPayment> listUserPayments(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException;

	int countUserPayment(UserSearchParam userSearchParam) throws UsageBillingException;

	UserPayment loadUserPaymentById(int paymentNo) throws UsageBillingException;

	void updateUserPayment(UserPayment userPayment) throws UsageBillingException;

	void deleteUserPayment(int paymentNo) throws UsageBillingException;

}
