package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartApiService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/cart/")
@CrossOrigin
public class CartApiController {

    @Autowired
    CartApiService cartApiService;

    @RequestMapping("getCartList/{userId}")
    List<CartInfo> getCartList(@PathVariable("userId") String userId){

        List<CartInfo> cartList = cartApiService.getCartList(userId);
        return cartList;
    }


    @RequestMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable("skuId") Long skuId,@PathVariable("isChecked") Integer isChecked){
        String userId = "1";// 用户身份认证系统
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        cartInfo.setIsChecked(isChecked);
        cartApiService.checkCart(cartInfo);
        return Result.ok();
    }

    @RequestMapping("cartList")
    public Result getCartList(){
        String userId = "1";// todo 用户身份认证系统
        List<CartInfo> cartInfos = cartApiService.getCartList(userId);
        return Result.ok(cartInfos);
    }

    @RequestMapping("addCart/{skuId}/{skuNum}")
    void addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum){
        String userId = "1";// todo 用户身份认证系统
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuNum(skuNum);

        cartApiService.addCart(cartInfo);
    }
}
