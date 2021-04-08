package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PayApiService;
import com.atguigu.gmall.rabbit.config.DelayedMqConfig;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayApiServiceImpl implements PayApiService {

    @Autowired
    OrderFeignClient orderFeignClient;
    @Autowired
    AlipayClient alipayClient;
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    RabbitService rabbitService;

    @Override
    public String tradePagePay(String userId, Long orderId) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);

        //保存支付数据
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //创建API对应的request
        alipayRequest.setReturnUrl("http://payment.gmall.com/api/payment/alipay/alipayCallBack");
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp"); //在公共参数中设置回跳和通知地址

        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", "0.01");
        map.put("subject", orderInfo.getOrderDetailList().get(0).getSkuName());

        alipayRequest.setBizContent(JSON.toJSONString(map)); //填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //记录支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setPaymentType(PaymentType.ALIPAY.getComment());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfoMapper.insert(paymentInfo);

        //通知消息队列，已提交支付，准备开始一个检查队列
        HashMap<String, Object> checkMap = new HashMap<>();
        checkMap.put("out_trade_no", paymentInfo.getOutTradeNo());
        checkMap.put("count", 7);
        rabbitService.sendDelay(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, JSON.toJSONString(checkMap), 5);

        return form;//向页面输入一个form表单的字符串
    }

    @Override
    public void callbackUpdate(PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", paymentInfo.getOutTradeNo());
        paymentInfoMapper.update(paymentInfo, queryWrapper);
        //通知订单系统支付完成
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
        orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
        orderInfo.setTradeBody(paymentInfo.getTradeNo());
        orderInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,JSON.toJSONString(orderInfo));
    }

    @Override
    public Map<String, Object> checkPayStatus(String out_order_no) {

        HashMap<String, Object> resultMap = new HashMap<>();

        //查询支付结果
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest(); //创建API对应的request
        alipayRequest.setReturnUrl("http://payment.gmall.com/api/payment/alipay/alipayCallBack");
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp"); //在公共参数中设置回跳和通知地址

        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", out_order_no);

        alipayRequest.setBizContent(JSON.toJSONString(map)); //填充业务参数
        try {
            AlipayTradeQueryResponse tradeQueryResponse = alipayClient.execute(alipayRequest);
            boolean success = tradeQueryResponse.isSuccess();
            if (success){
                //交易已创建
                String tradeStatus = tradeQueryResponse.getTradeStatus();
                resultMap.put("success",true);
                resultMap.put("tradeStatus",tradeStatus);
            }else {
                //交易未创建
                resultMap.put("success",false);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    @Override
    public String checkPayCallBack(String out_trade_no) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",out_trade_no);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(queryWrapper);
        String paymentStatus = paymentInfo.getPaymentStatus();
        return paymentStatus;
    }
}
