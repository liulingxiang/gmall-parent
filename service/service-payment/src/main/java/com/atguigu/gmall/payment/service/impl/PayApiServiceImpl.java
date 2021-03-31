package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.service.PayApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayApiServiceImpl implements PayApiService {

    @Autowired
    OrderFeignClient orderFeignClient;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public String tradePagePay(String userId, Long orderId) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);

        //保存支付数据
        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
        alipayRequest.setReturnUrl( "http://payment.gmall.com/alipayCallBack" );
        alipayRequest.setNotifyUrl( "http://domain.com/CallBack/notify_url.jsp" ); //在公共参数中设置回跳和通知地址

        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount","0.01");
        map.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());

        alipayRequest.setBizContent(JSON.toJSONString(map)); //填充业务参数
        String form= "" ;
        try  {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        }  catch  (AlipayApiException e) {
            e.printStackTrace();
        }

        return form;
    }
}