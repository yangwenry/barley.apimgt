package barley.apimgt.usage.billing.domain;

public class Invoice {

    private int invoiceNo;
    private int invoiceYear;
    private int invoiceMonth;
    private String userId;
    private int tenantId;
    private String userName;
    private String firstName;
    private String lastName;
    private String userCompany;
    private String userEmail;
    private String address1, address2, address3;
    private String paymentMethod;
    private int successCount;
    private int throttleCount;
    private String createdDate;

    private double subscriptionFee;
    private double successFee;
    private double throttleFee;
    private double totalFee;

    private double feePerSuccess;
    private double feePerThrottle;

    private String planName;
    private String planType;

    public int getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(int invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public int getInvoiceYear() {
        return invoiceYear;
    }

    public void setInvoiceYear(int invoiceYear) {
        this.invoiceYear = invoiceYear;
    }

    public int getInvoiceMonth() {
        return invoiceMonth;
    }

    public void setInvoiceMonth(int invoiceMonth) {
        this.invoiceMonth = invoiceMonth;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserCompany() {
        return userCompany;
    }

    public void setUserCompany(String userCompany) {
        this.userCompany = userCompany;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public double getSubscriptionFee() {
        return subscriptionFee;
    }

    public void setSubscriptionFee(double subscriptionFee) {
        this.subscriptionFee = subscriptionFee;
    }

    public double getSuccessFee() {
        return successFee;
    }

    public void setSuccessFee(double successFee) {
        this.successFee = successFee;
    }

    public double getThrottleFee() {
        return throttleFee;
    }

    public void setThrottleFee(double throttleFee) {
        this.throttleFee = throttleFee;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getFeePerSuccess() {
        return feePerSuccess;
    }

    public void setFeePerSuccess(double feePerSuccess) {
        this.feePerSuccess = feePerSuccess;
    }

    public double getFeePerThrottle() {
        return feePerThrottle;
    }

    public void setFeePerThrottle(double feePerThrottle) {
        this.feePerThrottle = feePerThrottle;
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
}
