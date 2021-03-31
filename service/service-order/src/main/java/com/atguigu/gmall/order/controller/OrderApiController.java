package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderApiService;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    OrderApiService orderApiService;

    @RequestMapping("getOrderInfoById/{orderId}")
    OrderInfo getOrderInfoById(@PathVariable("orderId") Long orderId) {
        return orderApiService.getOrderInfoById(orderId);
    }

    @RequestMapping("getOrderDetails")
    List<OrderDetail> getOrderDetails(HttpServletRequest request) {

        String userId = request.getHeader("userId");
        List<OrderDetail> list = orderApiService.getOrderDetails(userId);
        return list;
    }

    @RequestMapping("genTradeNo")
    String genTradeNo(HttpServletRequest request) {
        String userId = request.getHeader("userId");
        String tradeNo = orderApiService.genTradeNo(userId);
        return tradeNo;
    }

    @RequestMapping("/auth/submitOrder")
    Result submitOrder(HttpServletRequest request, @RequestBody OrderInfo orderInfo, String tradeNo) {
        String userId = request.getHeader("userId");
        boolean flag = orderApiService.checkTradeNo(userId, tradeNo);

        if (flag) {
            // 保存订单信息,删除购物车信息
            orderInfo.setUserId(Long.parseLong(userId));
            orderApiService.saveOrder(orderInfo);
            return Result.ok(orderInfo.getId());
        } else {
            return Result.fail();
        }
    }
}
