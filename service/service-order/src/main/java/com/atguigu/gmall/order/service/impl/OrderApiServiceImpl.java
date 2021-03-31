package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderApiService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class OrderApiServiceImpl implements OrderApiService {
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Override
    public List<OrderDetail> getOrderDetails(String userId) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        // 结算的商品数据来自购物车，是购物车中被选中的sku转化
        List<CartInfo> cartInfos = cartFeignClient.getCartList(userId);
        for (CartInfo cartInfo : cartInfos) {
            if (cartInfo.getIsChecked() == 1) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setHasStock("1");
                orderDetails.add(orderDetail);
            }
        }
        return orderDetails;
    }

    @Override
    public String genTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("user:" + userId + ":tradeNo", tradeNo, 30, TimeUnit.MINUTES);
        return tradeNo;
    }

    @Override
    public boolean checkTradeNo(String userId, String tradeNo) {
        boolean flag = false;

        String tradeNoCache = (String) redisTemplate.opsForValue().get("user:" + userId + ":tradeNo");
        if (tradeNoCache != null && tradeNoCache.equals(tradeNo)) {
            flag = true;
            redisTemplate.delete("user:" + userId + ":tradeNo");
        }
        return flag;
    }

    @Override
    public void saveOrder(OrderInfo orderInfo) {

        orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
        orderInfo.setOrderStatus(ProcessStatus.UNPAID.getComment());
        orderInfo.setImgUrl(orderInfo.getOrderDetailList().get(0).getImgUrl());
        orderInfo.setCreateTime(new Date());
        //24小时后过期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        Date time = calendar.getTime();
        orderInfo.setExpireTime(time);
        // 外部订单号，毫秒时间戳+时间字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String outTradeNo = "atguigu" + sdf.format(new Date()) + System.currentTimeMillis();
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setOrderComment("谷粒订单");
        orderInfo.setTotalAmount(getTotalAmount(orderInfo.getOrderDetailList()));

        orderInfoMapper.insert(orderInfo);
        Long orderId = orderInfo.getId();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);
        }
        // 保存完毕订单后需要调用cart服务，删除已经保存订单的购物车数据
        // cartFeignClient.delOrderCart();
    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);

        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        List<OrderDetail> list = orderDetailMapper.selectList(queryWrapper);

        orderInfo.setOrderDetailList(list);
        return orderInfo;
    }

    private BigDecimal getTotalAmount(List<OrderDetail> orderDetailList) {
        BigDecimal bigDecimal = new BigDecimal("0");
        if (orderDetailList != null && orderDetailList.size() > 0) {
            for (OrderDetail orderDetail : orderDetailList) {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                if (orderPrice != null) {
                    bigDecimal = bigDecimal.add(orderPrice);
                }
            }
        }

        return null;
    }
}
