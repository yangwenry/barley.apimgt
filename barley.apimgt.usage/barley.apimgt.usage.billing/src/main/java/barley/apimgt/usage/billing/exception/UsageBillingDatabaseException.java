package barley.apimgt.usage.billing.exception;

public class UsageBillingDatabaseException extends Exception {
    public UsageBillingDatabaseException(String message, Exception e) {
        super(message, e);
    }
}
