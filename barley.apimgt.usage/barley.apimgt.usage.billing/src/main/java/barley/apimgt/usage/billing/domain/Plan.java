package barley.apimgt.usage.billing.domain;

public class Plan {
    private int planNo;
    private String planName;
    private String planType;

    private String quota;
    // 요청 당 금액
    private double feePerRequest;
    // 구독료
    private double subscriptionFee;
    // 수수료
    private double feeRate;

    public enum PLAN_TYPES {STANDARD, USAGE}

    public int getPlanNo() {
        return planNo;
    }

    public void setPlanNo(int planNo) {
        this.planNo = planNo;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public double getFeePerRequest() {
        return feePerRequest;
    }

    public void setFeePerRequest(double feePerRequest) {
        this.feePerRequest = feePerRequest;
    }

    public double getSubscriptionFee() {
        return subscriptionFee;
    }

    public void setSubscriptionFee(double subscriptionFee) {
        this.subscriptionFee = subscriptionFee;
    }

    public double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(double feeRate) {
        this.feeRate = feeRate;
    }
}
