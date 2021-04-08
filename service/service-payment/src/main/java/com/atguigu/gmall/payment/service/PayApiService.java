package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PayApiService {
    String tradePagePay(String userId, Long orderId);

    void callbackUpdate(PaymentInfo paymentInfo);

    Map<String, Object> checkPayStatus(String out_order_no);

    String checkPayCallBack(String out_trade_no);
}
