package com.atguigu.gmall.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.ActivityApiService;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ActivityApiServiceImpl implements ActivityApiService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RabbitService rabbitService;
    @Autowired
    OrderFeignClient orderFeignClient;

    @Override
    public void putGoods(Long skuId) {
        // 查出数据库中的秒杀商品数据
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(queryWrapper);

        if (seckillGoods != null) {
            // 将库存元素放入缓存库存集合，有几个库存就放几次
            Integer stockCount = seckillGoods.getStockCount();
            for (int i = 0; i < stockCount; i++) {
                redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).leftPush(seckillGoods.getSkuId());
            }
            // 将库存的详情放到商品列表中
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGoods.getSkuId() + "", seckillGoods);
        }
        // redis发布商品已经上架的通知
        redisTemplate.convertAndSend("seckillpush", seckillGoods.getSkuId() + ":1");
    }

    @Override
    public SeckillGoods getGoods(Long skuId) {

        SeckillGoods getGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");
        return getGoods;
    }

    @Override
    public List<SeckillGoods> findAll() {
        List list = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return list;
    }

    @Override
    public SeckillGoods getItem(Long skuId) {
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");
        return seckillGoods;
    }

    @Override
    public void seckillStock(UserRecode userRecode) {
        Integer result = (Integer) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + userRecode.getSkuId()).rightPop();
        if (result != null && result > 0) {
            //抢到了,生成预订单,等待检查
            OrderRecode orderRecode = new OrderRecode();
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(userRecode.getSkuId() + "");
            orderRecode.setNum(1);
            orderRecode.setUserId(userRecode.getUserId());
            orderRecode.setSeckillGoods(seckillGoods);
            redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(userRecode.getUserId()+"",orderRecode);
        }else {
            //没货了，消息队列通知
            redisTemplate.convertAndSend("seckillpush",userRecode.getSkuId()+":0");
        }
    }

    @Override
    public boolean seckillLock(String skuId, String userId) {
        boolean flag = redisTemplate.opsForValue().setIfAbsent("seckill:" + userId + ":" + skuId + ":lock",1,5, TimeUnit.SECONDS);
        return flag;
    }

    @Override
    public void seckillOrder(String skuId, String userId) {
        UserRecode userRecode = new UserRecode();
        userRecode.setSkuId(Long.valueOf(skuId));
        userRecode.setUserId(userId);
        //发送消息通知抢购
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER, MqConst.ROUTING_SECKILL_USER, JSON.toJSONString(userRecode));
    }

    @Override
    public String getUserOrder(String userId) {
        String orderId = (String)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
        return orderId;
    }

    @Override
    public OrderRecode getOrderRecode(String userId) {
        OrderRecode orderRecode = (OrderRecode)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        return orderRecode;
    }

    @Override
    public String submitOrder(OrderInfo orderInfo) {
        String orderId = orderFeignClient.saveOrder(orderInfo);
        // 删除预订单
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(orderInfo.getUserId()+"");
        // 生成已下单用户信息
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(orderInfo.getUserId()+"",orderId);
        return orderId;
    }
}
