package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;

public interface OrderApiService {
    List<OrderDetail> getOrderDetails(String userId);

    String genTradeNo(String userId);

    boolean checkTradeNo(String userId, String tradeNo);

    void saveOrder(OrderInfo orderInfo);

    OrderInfo getOrderInfoById(Long orderId);
}
