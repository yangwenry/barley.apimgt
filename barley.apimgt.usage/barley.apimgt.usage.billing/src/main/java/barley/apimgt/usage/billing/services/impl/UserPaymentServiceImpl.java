package barley.apimgt.usage.billing.services.impl;

import barley.apimgt.usage.billing.dao.UserPaymentDao;
import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.services.UserPaymentService;
import barley.apimgt.usage.billing.vo.UserSearchParam;

import java.util.List;

public class UserPaymentServiceImpl implements UserPaymentService {
    private UserPaymentDao userPaymentDao;

    public UserPaymentServiceImpl(UserPaymentDao userPaymentDao) {
        this.userPaymentDao = userPaymentDao;
    }

    public UserPaymentDao getUserPaymentDao() {
        return userPaymentDao;
    }

    public void setUserPaymentDao(UserPaymentDao userPaymentDao) {
        this.userPaymentDao = userPaymentDao;
    }

    @Override
    public void createUserPayment(UserPayment userPayment) throws UsageBillingException {
        userPaymentDao.addUserPayment(userPayment);
    }

    @Override
    public List<UserPayment> listUserPayments() throws UsageBillingException {
        return userPaymentDao.loadUserPayments();
    }

    @Override
    public List<UserPayment> listUserPayments(String userId, int tenantId) throws UsageBillingException {
        return userPaymentDao.loadUserPayments(userId, tenantId);
    }

    @Override
    public List<UserPayment> listUserPayments(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException {
        return userPaymentDao.loadUserPayments(page, count, userSearchParam);
    }

    @Override
    public int countUserPayment(UserSearchParam userSearchParam) throws UsageBillingException {
        return userPaymentDao.countUserPayment(userSearchParam);
    }

    @Override
    public UserPayment loadUserPaymentById(int paymentNo) throws UsageBillingException {
        return userPaymentDao.loadUserPaymentByID(paymentNo);
    }

    @Override
    public void updateUserPayment(UserPayment userPayment) throws UsageBillingException {
        userPaymentDao.updateUserPayment(userPayment);
    }

    @Override
    public void deleteUserPayment(int paymentNo) throws UsageBillingException {
        userPaymentDao.deleteUserPayment(paymentNo);
    }
}
