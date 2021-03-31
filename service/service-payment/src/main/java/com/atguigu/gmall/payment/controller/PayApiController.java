package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.payment.service.PayApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/payment")
public class PayApiController {

    @Autowired
    PayApiService payApiService;

    @RequestMapping("alipay/submit/{orderId}")
    public String alipay(HttpServletRequest request,@PathVariable("orderId") Long orderId){

        String userId = request.getHeader("userId");

        String form = payApiService.tradePagePay(userId,orderId);
        // 向页面输入一个form表单的字符串
        return form;
    }
}
