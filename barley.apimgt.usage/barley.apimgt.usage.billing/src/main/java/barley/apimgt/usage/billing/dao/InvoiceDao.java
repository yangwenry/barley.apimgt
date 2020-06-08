package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.exception.UsageBillingException;
import barley.apimgt.usage.billing.vo.UserSearchParam;

import java.util.List;

/**
 * Data access object interface to work with User entity database operations.
 *
 * @author Arthur Rukshan
 */
public interface InvoiceDao {

    Invoice loadInvoiceByID(int invoiceNo) throws UsageBillingException;

    List<Invoice> loadInvoices() throws UsageBillingException;

    List<Invoice> loadInvoices(int year, int month, int tenantId) throws UsageBillingException;

    List<Invoice> loadInvoices(int page, int count, UserSearchParam userSearchParam) throws UsageBillingException;

    int countInvoice(UserSearchParam userSearchParam) throws UsageBillingException;

    void addInvoice(Invoice invoice) throws UsageBillingException;

    void updateInvoice(Invoice invoice) throws UsageBillingException;

    void deleteInvoice(int invoiceNo) throws UsageBillingException;

    void deleteInvoice(int year, int month) throws UsageBillingException;
}
