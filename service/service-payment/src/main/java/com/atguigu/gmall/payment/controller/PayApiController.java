package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.service.PayApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("api/payment")
public class PayApiController {

    @Autowired
    PayApiService payApiService;

    @RequestMapping("alipay/alipayCallBack")
    public String alipayCallBack(HttpServletRequest request){

        String callbackContent = request.getQueryString();// 支付回调的参数

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(request.getParameter("out_trade_no"));
        paymentInfo.setTradeNo(request.getParameter("trade_no"));
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setCallbackContent(callbackContent);

        //修改支付数据
        payApiService.callbackUpdate(paymentInfo);

        String form = "<form action='http://payment.gmall.com/success'></form><script>document.forms[0].submit();</script>";

        return form;
    }

    @RequestMapping("alipay/submit/{orderId}")
    public String alipay(HttpServletRequest request,@PathVariable("orderId") Long orderId){

        String userId = request.getHeader("userId");

        String form = payApiService.tradePagePay(userId,orderId);
        // 向页面输入一个form表单的字符串
        return form;
    }
}
