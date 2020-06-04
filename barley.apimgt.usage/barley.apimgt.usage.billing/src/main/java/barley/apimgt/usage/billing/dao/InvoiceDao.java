package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.exception.BillingException;

import java.util.List;

/**
 * Data access object interface to work with User entity database operations.
 *
 * @author Arthur Rukshan
 */
public interface InvoiceDao {

    Invoice loadInvoiceByID(int invoiceNo) throws BillingException;

    List<Invoice> loadInvoices() throws BillingException;

    void addInvoice(Invoice invoice) throws BillingException;

    void updateInvoice(Invoice invoice) throws BillingException;

    void deleteInvoice(int invoiceNo) throws BillingException;
}
