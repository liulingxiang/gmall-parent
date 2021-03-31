package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    OrderFeignClient orderFeignClient;
    @Autowired
    UserFeignClient userFeignClient;

    @RequestMapping("trade.html")
    public String trade(Model model){

        // 网关会提前拦截请求，必须登录状态才能访问trade功能

        // 用户收获列表集合
        List<UserAddress> userAddresses = userFeignClient.getUserAddresses();

        // 商品的订单详情orderDetail集合
        List<OrderDetail> orderDetails = orderFeignClient.getOrderDetails();

        model.addAttribute("userAddressList",userAddresses);
        model.addAttribute("detailArrayList",orderDetails);
        model.addAttribute("totalAmount",getTotalAmount(orderDetails));

        // 生成tradeNo，用来防止用户重复提交订单
        String tradeNo = orderFeignClient.genTradeNo();
        model.addAttribute("tradeNo",tradeNo);

        return "order/trade.html";
    }

    private Object getTotalAmount(List<OrderDetail> orderDetails) {

        BigDecimal bigDecimal = new BigDecimal("0");
        if (orderDetails!=null&&orderDetails.size()>0){
            for (OrderDetail orderDetail : orderDetails) {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                if (orderDetail!=null){
                    bigDecimal = bigDecimal.add(orderPrice);
                }
            }
        }
        return bigDecimal;
    }
}
