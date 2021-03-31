package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CartController {

    @Autowired
    CartFeignClient cartFeignClient;

    @RequestMapping("cart/cart.html")
    public String CartList(){
        return "cart/index";
    }

    @RequestMapping("addCart.html")
    public String addCart(Long skuId,Long skuNum){

        // 添加购物车的业务调用
        cartFeignClient.addCart(skuId,skuNum);
        return "redirect:http://cart.gmall.com/cart/addCart.html?k=v&k=v";
    }
}
