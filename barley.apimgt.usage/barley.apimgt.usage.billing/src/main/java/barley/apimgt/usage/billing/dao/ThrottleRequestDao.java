/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package barley.apimgt.usage.billing.dao;

import barley.apimgt.usage.billing.domain.Invoice;
import barley.apimgt.usage.billing.domain.Plan;
import barley.apimgt.usage.billing.domain.UserPayment;
import barley.apimgt.usage.billing.exception.BillingException;
import barley.apimgt.usage.billing.vo.ThrottleRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ThrottleRequestDao {

    private PlanDao planDao;

    public ThrottleRequestDao(PlanDao planDao) {
        this.planDao = planDao;
    }

    public PlanDao getPlanDao() {
        return planDao;
    }

    public void setPlanDao(PlanDao planDao) {
        this.planDao = planDao;
    }

    private Invoice getInvoice(int success, int throttle, String planName, UserPayment userPayment) throws BillingException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Calendar calendar = Calendar.getInstance();
        String billDate = dateFormat.format(calendar.getTime());
        int invoiceYear = calendar.get(Calendar.YEAR);
        int invoiceMonth = calendar.get(Calendar.MONTH);
        //calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        //String dueDate = dateFormat.format(calendar.getTime());

        Plan plan = planDao.loadPlanByPlanName(planName);

        throttle = getThrottleCount(plan, success, throttle);

        double subscriptionFee = plan.getSubscriptionFee();
        double successFee = getSuccessRequestFee(plan, success);
        double throttleFee = getThrottleRequestFee(plan, throttle);
        double totalFee = subscriptionFee + successFee + throttleFee;

        double feePerRequest = getPerSuccessFee(plan);
        double feePerThrottle = getPerThrottleFee(plan);

        //int ran = (int) (Math.random() * 1000);

        Invoice invoice = new Invoice();
        invoice.setAddress1(userPayment.getAddress1());
        invoice.setAddress2(userPayment.getAddress2());
        invoice.setAddress3(userPayment.getAddress3());
        invoice.setCreatedDate(billDate);
        invoice.setInvoiceYear(invoiceYear);
        invoice.setInvoiceMonth(invoiceMonth);
        //invoice.setInvoiceNo(ran);
        invoice.setPaymentMethod(userPayment.getCardType());
        invoice.setSubscriptionFee(plan.getSubscriptionFee());
        invoice.setSuccessCount(success);
        invoice.setSuccessFee(successFee);
        invoice.setThrottleCount(throttle);
        invoice.setThrottleFee(throttleFee);
        invoice.setTotalFee(totalFee);
        invoice.setUserCompany(userPayment.getCompany());
        invoice.setUserEmail(userPayment.getUserEmail());
        invoice.setUserName(userPayment.getUserName());
        invoice.setFirstName(userPayment.getFirstName());
        invoice.setLastName(userPayment.getLastName());
        invoice.setFeePerSuccess(feePerRequest);
        invoice.setFeePerThrottle(feePerThrottle);
        invoice.setPlanName(plan.getPlanName());
        invoice.setPlanType(plan.getPlanType());
        return invoice;
    }

    public Invoice generateInvoice(String planName, UserPayment userPayment, ThrottleRequest throttleRequest) throws BillingException {
        int sCount = throttleRequest.getSuccessCount();
        int tCount = throttleRequest.getThrottleCount();

        Invoice result = getInvoice(sCount, tCount, planName, userPayment);
        return result;
    }

    private double getSuccessRequestFee(Plan plan, int success) {
        if (plan.getPlanType().equals("STANDARD")) {
            return 0.0;
        } else if (plan.getPlanType().equals("USAGE")) {
            return success * plan.getFeePerRequest();
        } else {
            return 0.0;
        }
    }

    private double getThrottleRequestFee(Plan plan, int throttle) {
        if (plan.getPlanType().equals("STANDARD")) {
            return throttle * plan.getFeePerRequest();
        } else if (plan.getPlanType().equals("USAGE")) {
            return 0.0;
        } else {
            return 0.0;
        }
    }

    private int getThrottleCount(Plan plan, int success, int throttle) {
        if (plan.getPlanType().equals("STANDARD")) {
            int diff = success - Integer.parseInt(plan.getQuota());
            if (diff > 0) {
                return diff;
            } else {
                return success;
            }
        } else if (plan.getPlanType().equals("USAGE")) {
            return throttle;
        } else {
            return throttle;
        }
    }

    private double getPerSuccessFee(Plan plan) {
        if (plan.getPlanType().equals("STANDARD")) {
            return 0.0;
        } else {
            return plan.getFeePerRequest();
        }
    }

    private double getPerThrottleFee(Plan plan) {
        if (plan.getPlanType().equals("STANDARD")) {
            return plan.getFeePerRequest();
        } else {
            return 0.0;
        }
    }
}
