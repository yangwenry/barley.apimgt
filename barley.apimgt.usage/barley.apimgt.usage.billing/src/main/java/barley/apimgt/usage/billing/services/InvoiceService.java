package barley.apimgt.usage.billing.services;

import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.services.impl.ThrottleRequestService;
import barley.apimgt.usage.billing.vo.ThrottleRequestVO;
import barley.apimgt.usage.billing.vo.UserSearchParam;

import java.util.List;

/**
 * Service providing service methods to work with user data and entity.
 *
 * @author Arthur rukshan
 */
public interface InvoiceService {

    Invoice createInvoice(String planName, int year, int month, UserPayment userPayment, ThrottleRequestVO throttleRequest) throws UsageBillingException;

    List<Invoice> listInvoices() throws UsageBillingException;

    List<Invoice> listInvoices(int year, int month, int tenantId) throws UsageBillingException;

    List<Invoice> listInvoices(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException;

    int countInvoice(UserSearchParam userSearchParam) throws UsageBillingException;

    Invoice getInvoiceById(int invoiceNo) throws UsageBillingException;

    void updateInvoice(Invoice invoice) throws UsageBillingException;

    void deleteInvoice(int invoiceNo) throws UsageBillingException;

    void deleteInvoice(int year, int month) throws UsageBillingException;

    double getAllTotalFee(int year, int month, int tenantId) throws UsageBillingException;

    void setThrottleRequestDao(ThrottleRequestService throttleRequestService);
}
