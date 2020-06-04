package barley.apimgt.usage.billing.services;

import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.BillingException;
import barley.apimgt.usage.billing.vo.ThrottleRequest;

import java.util.List;

/**
 * Service providing service methods to work with user data and entity.
 *
 * @author Arthur rukshan
 */
public interface InvoiceService {

    Invoice createInvoice(UserPayment userPayment, ThrottleRequest throttleRequest) throws BillingException;

    List<Invoice> listInvoices() throws BillingException;

    Invoice getInvoiceById(int invoiceNo) throws BillingException;
}
