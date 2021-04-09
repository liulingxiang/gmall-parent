package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.atguigu.gmall.util.MD5;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping
public class SeckillController {

    @Autowired
    ActivityFeignClient activityFeignClient;
    @Autowired
    UserFeignClient userFeignClient;

    @RequestMapping("seckill/trade.html")
    public String trade(Model model, String skuId, String skuIdStr, HttpServletRequest request){
        String userId = request.getHeader("userId");

        // 查询预订单信息
        OrderRecode orderRecode = activityFeignClient.getOrderRecode();
        // 查询收获信息
        List<UserAddress> userAddresses = userFeignClient.getUserAddresses();
        //将与订单转化为正式订单详情信息
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

        List<OrderDetail> orderDetails = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuNum(1);
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetails.add(orderDetail);

        model.addAttribute("userAddressList",userAddresses);
        model.addAttribute("detailArrayList",orderDetails);
        model.addAttribute("totalAmount",getTotalAmount(orderDetails));

        return "seckill/trade";
    }
    private BigDecimal getTotalAmount(List<OrderDetail> orderDetails) {
        BigDecimal bigDecimal = new BigDecimal("0");
        if (orderDetails!=null&&orderDetails.size()>0){
            for (OrderDetail orderDetail : orderDetails) {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                if (orderPrice!=null){
                    bigDecimal = bigDecimal.add(orderPrice);
                }
            }
        }
        return bigDecimal;
    }

    @RequestMapping("seckill/queue.html")
    public String queue(Model model, String skuIdStr,String skuId, HttpServletRequest request){

        String encrypt = MD5.encrypt(skuId, request);
        String userId = request.getHeader("userId");
        if (StringUtils.isNotEmpty(encrypt)&&encrypt.equals(skuIdStr)){
            model.addAttribute("skuId",skuId);
            model.addAttribute("skuIdStr",skuIdStr);
            return "seckill/queue";
        }else {
            model.addAttribute("message","请求非法");
            return "seckill/fail";
        }
    }

    @RequestMapping("seckill.html")
    public String findAll(Model model) {
        List<SeckillGoods> seckillGoods = activityFeignClient.findAll();
        model.addAttribute("list",seckillGoods);
        return "seckill/index";
    }

    @RequestMapping("seckill/{skuId}.html")
    public String getItem(Model model,@PathVariable("skuId") Long skuId){
        String status = activityFeignClient.getCacheHelper(skuId);
        if (StringUtils.isNotEmpty(status) &&status.equals("1")){
            SeckillGoods seckillGood = activityFeignClient.getItem(skuId);
            model.addAttribute("item",seckillGood);
            return "seckill/item";
        }else {
            model.addAttribute("message","秒杀商品售罄，{倒计时}时间后进行第二轮");
            return "seckill/fail";
        }
    }
}
