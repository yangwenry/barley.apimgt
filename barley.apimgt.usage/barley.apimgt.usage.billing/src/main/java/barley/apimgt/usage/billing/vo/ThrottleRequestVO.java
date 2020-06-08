package barley.apimgt.usage.billing.vo;

public class ThrottleRequestVO {
    private int successCount;
    private int throttleCount;

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getThrottleCount() {
        return throttleCount;
    }

    public void setThrottleCount(int throttleCount) {
        this.throttleCount = throttleCount;
    }
}
