package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;

public interface ActivityApiService {
    void putGoods(Long skuId);

    SeckillGoods getGoods(Long skuId);

    List<SeckillGoods> findAll();

    SeckillGoods getItem(Long skuId);

    void seckillStock(UserRecode userRecode);

    boolean seckillLock(String skuId, String userId);

    void seckillOrder(String skuId, String userId);

    String getUserOrder(String userId);

    OrderRecode getOrderRecode(String userId);

    String submitOrder(OrderInfo orderInfo);
}
