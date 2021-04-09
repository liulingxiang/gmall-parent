package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "service-activity")
public interface ActivityFeignClient {

    @RequestMapping("api/activity/seckill/auth/getSeckillSkuIdStr/{skuId}")
    String getSeckillSkuIdStr(@PathVariable("skuId") String skuId);

    @RequestMapping("api/activity/seckill/findAll")
    List<SeckillGoods> findAll();

    @RequestMapping("api/activity/seckill/getCacheHelper/{skuId}")
    String getCacheHelper(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/activity/seckill/getItem/{skuId}")
    SeckillGoods getItem(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/activity/seckill/auth/getOrderRecode")
    OrderRecode getOrderRecode();

}
