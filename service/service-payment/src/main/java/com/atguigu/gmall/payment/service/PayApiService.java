package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

public interface PayApiService {
    String tradePagePay(String userId, Long orderId);

    void callbackUpdate(PaymentInfo paymentInfo);
}
