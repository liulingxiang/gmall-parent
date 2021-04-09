package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.ActivityApiService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.result.ResultCodeEnum;
import com.atguigu.gmall.util.MD5;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/activity/seckill")
public class ActivityApiController {

    @Autowired
    ActivityApiService activityApiService;
    @Autowired
    RabbitService rabbitService;

    @RequestMapping("/auth/submitOrder")
    public Result submitOrder(HttpServletRequest request, @RequestBody OrderInfo orderInfo){
        String userId = request.getHeader("userId");

        // 保存订单信息,删除购物车信息
        orderInfo.setUserId(Long.parseLong(userId));
        orderInfo.setOutTradeNo("atguigu"+System.currentTimeMillis()+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
        orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
        orderInfo.setTotalAmount(getTotalAmount(orderInfo.getOrderDetailList()));
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        orderInfo.setPaymentWay(PaymentWay.ONLINE.getComment());
        orderInfo.setImgUrl(orderInfo.getOrderDetailList().get(0).getImgUrl());

        String orderId = activityApiService.submitOrder(orderInfo);

        return Result.ok(orderId);
    }

    private BigDecimal getTotalAmount(List<OrderDetail> orderDetailList) {
        BigDecimal bigDecimal = new BigDecimal("0");
        if (orderDetailList!=null&&orderDetailList.size()>0){
            for (OrderDetail orderDetail : orderDetailList) {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                if (orderPrice!=null){
                    bigDecimal = bigDecimal.add(orderPrice);
                }
            }
        }
        return bigDecimal;
    }

    @RequestMapping("auth/getOrderRecode")
    OrderRecode getOrderRecode(HttpServletRequest request){
        String userId = request.getHeader("userId");
        OrderRecode orderRecode = activityApiService.getOrderRecode(userId);
        return orderRecode;
    }

    @RequestMapping("/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") String skuId, String skuIdStr, HttpServletRequest request) {
        String userId = request.getHeader("userId");

        String orderId = activityApiService.getUserOrder(userId);
        if (StringUtils.isNotEmpty(orderId)) {
            // 已下单
            return Result.build(null, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        OrderRecode orderRecode = activityApiService.getOrderRecode(userId);
        if (orderRecode != null) {
            // 抢购成功
            return Result.build(null, ResultCodeEnum.SECKILL_SUCCESS);
        }
        String status = CacheHelper.get(skuId + "");
        if (status!=null&&status.equals("0")){
            // 已售罄
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }
        //排队中
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    @RequestMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") String skuId, String skuIdStr, HttpServletRequest request) {
        String userId = request.getHeader("userId");

        boolean flag = activityApiService.seckillLock(skuId, userId);
        if (flag) {
            if (skuIdStr != null && userId != null) {
                String status = CacheHelper.get(skuId);
                if (StringUtils.isEmpty(status)) {
                    //请求不合法
                    return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
                }
                if (status.equals("1")) {
                    //有货
                    // 将请求放入缓冲区，消息队列
                    activityApiService.seckillOrder(skuId, userId);
                } else {
                    //售罄
                    return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
                }
            } else {
                //请求不合法
                return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
            }
        } else {
            //请求不合法
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        return Result.ok();
    }

    //前端页面ajax异步调用
    @RequestMapping("auth/getSeckillSkuIdStr/{skuId}")
    Result getSeckillSkuIdStr(@PathVariable("skuId") String skuId, HttpServletRequest request) {
        String skuIdStr = MD5.encrypt(skuId, request);
        String userId = request.getParameter("userId");
        return Result.ok(skuIdStr);
    }

    @RequestMapping("getCacheHelper/{skuId}")
    String getCacheHelper(@PathVariable("skuId") Long skuId) {
        String status = CacheHelper.get(skuId + "");
        return status;
    }

    @RequestMapping("getItem/{skuId}")
    SeckillGoods getItem(@PathVariable("skuId") Long skuId) {
        SeckillGoods seckillGoods = activityApiService.getItem(skuId);
        return seckillGoods;
    }

    @RequestMapping("findAll")
    List<SeckillGoods> findAll() {
        List<SeckillGoods> seckillGoods = activityApiService.findAll();
        return seckillGoods;
    }

    @RequestMapping("getStatus")
    public Result getStatus() {

        return Result.ok(CacheHelper.cacheMap);
    }

    @RequestMapping("putGoods/{skuId}")
    public Result putGoods(@PathVariable("skuId") Long skuId) {
        activityApiService.putGoods(skuId);
        return Result.ok();
    }

    @RequestMapping("getGoods/{skuId}")
    public SeckillGoods getGoods(@PathVariable("skuId") Long skuId) {
        SeckillGoods seckillGoods = activityApiService.getGoods(skuId);
        return seckillGoods;
    }

}
