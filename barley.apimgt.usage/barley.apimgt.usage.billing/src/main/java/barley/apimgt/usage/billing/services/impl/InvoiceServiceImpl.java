package barley.apimgt.usage.billing.services.impl;

import barley.apimgt.usage.billing.dao.InvoiceDao;
import barley.apimgt.usage.billing.dao.ThrottleRequestDao;
import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.BillingException;
import barley.apimgt.usage.billing.services.InvoiceService;
import barley.apimgt.usage.billing.vo.ThrottleRequest;

import java.util.List;

/**
 * Service providing service methods to work with user data and entity.
 *
 * @author Arthur Rukshan
 */
public class InvoiceServiceImpl implements InvoiceService {

    private ThrottleRequestDao throttleRequestDao;
    private InvoiceDao invoiceDao;
    private String planName;

    public ThrottleRequestDao getThrottleRequestDao() {
        return throttleRequestDao;
    }

    public void setThrottleRequestDao(ThrottleRequestDao throttleRequestDao) {
        this.throttleRequestDao = throttleRequestDao;
    }

    public InvoiceDao getInvoiceDao() {
        return invoiceDao;
    }

    public void setInvoiceDao(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Invoice createInvoice(UserPayment userPayment, ThrottleRequest throttleRequest) throws BillingException {
        Invoice result = throttleRequestDao.generateInvoice(planName, userPayment, throttleRequest);
        invoiceDao.addInvoice(result);
        return result;
    }

    public List<Invoice> listInvoices() throws BillingException {
        return invoiceDao.loadInvoices();
    }

    public Invoice getInvoiceById(int invoiceNo) throws BillingException {
        return invoiceDao.loadInvoiceByID(invoiceNo);
    }
}
