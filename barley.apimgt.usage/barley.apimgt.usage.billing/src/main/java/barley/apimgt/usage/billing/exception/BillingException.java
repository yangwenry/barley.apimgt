package barley.apimgt.usage.billing.exception;

import java.sql.SQLException;

public class BillingException extends Exception {
    public BillingException(String msg, SQLException e) {
        super(msg, e);
    }
}
