package barley.apimgt.usage.billing.services.impl;

import barley.apimgt.usage.billing.dao.InvoiceDao;
import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.services.InvoiceService;
import barley.apimgt.usage.billing.vo.ThrottleRequestVO;
import barley.apimgt.usage.billing.vo.UserSearchParam;

import java.util.List;

/**
 * Service providing service methods to work with user data and entity.
 *
 * @author Arthur Rukshan
 */
public class InvoiceServiceImpl implements InvoiceService {

    private ThrottleRequestService throttleRequestService;
    private InvoiceDao invoiceDao;

    public InvoiceServiceImpl(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    public void setThrottleRequestDao(ThrottleRequestService throttleRequestService) {
        this.throttleRequestService = throttleRequestService;
    }

    public InvoiceDao getInvoiceDao() {
        return invoiceDao;
    }

    public void setInvoiceDao(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    public Invoice createInvoice(String planName, int year, int month, UserPayment userPayment, ThrottleRequestVO throttleRequest) throws UsageBillingException {
        // 기준 년/월에 이미 생성했는지 확인
        List<Invoice> invoices = invoiceDao.loadInvoices(year, month, userPayment.getTenantId());
        if(invoices != null && invoices.size() > 0) {
            String msg = "already exists invoice. year: " + year + ", month: " + month;
            throw new UsageBillingException(msg);
        }

        Invoice result = throttleRequestService.generateInvoice(planName, year, month, userPayment, throttleRequest);
        invoiceDao.addInvoice(result);
        return result;
    }

    public List<Invoice> listInvoices() throws UsageBillingException {
        return invoiceDao.loadInvoices();
    }

    @Override
    public List<Invoice> listInvoices(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException {
        return invoiceDao.loadInvoices(page, count, userSearchParam);
    }

    @Override
    public int countInvoice(UserSearchParam userSearchParam) throws UsageBillingException {
        return invoiceDao.countInvoice(userSearchParam);
    }

    public Invoice getInvoiceById(int invoiceNo) throws UsageBillingException {
        return invoiceDao.loadInvoiceByID(invoiceNo);
    }

    @Override
    public void updateInvoice(Invoice invoice) throws UsageBillingException {
        invoiceDao.updateInvoice(invoice);
    }

    @Override
    public void deleteInvoice(int invoiceNo) throws UsageBillingException {
        invoiceDao.deleteInvoice(invoiceNo);
    }

    @Override
    public void deleteInvoice(int year, int month) throws UsageBillingException {
        invoiceDao.deleteInvoice(year, month);
    }

    @Override
    public double getAllTotalFee(int year, int month, int tenantId) throws UsageBillingException {
        List<Invoice> invoices = invoiceDao.loadInvoices(year, month, tenantId);
        double totalFee = 0.0;
        for(Invoice invoice : invoices) {
            totalFee += invoice.getTotalFee();
        }
        return totalFee;
    }
}
