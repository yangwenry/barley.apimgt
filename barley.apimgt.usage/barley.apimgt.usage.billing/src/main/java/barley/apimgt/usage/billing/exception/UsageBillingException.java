package barley.apimgt.usage.billing.exception;

public class UsageBillingException extends Exception {
    public UsageBillingException(String message, Exception e) {
        super(message, e);
    }

    public UsageBillingException(String message) {
        super(message);
    }
}
