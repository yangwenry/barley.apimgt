package barley.apimgt.usage.billing.exception;

import java.sql.SQLException;

public class UsageBillingException extends Exception {
    public UsageBillingException(String msg, SQLException e) {
        super(msg, e);
    }

    public UsageBillingException(String msg) {
        super(msg);
    }
}
