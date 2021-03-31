package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class ItemController {
    @Autowired
    ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model Model){
        // 分类

        // 商品

        // 价格

        // 销售属性

        // 图片

        Map<String,Object> map = itemFeignClient.item(skuId);
        Model.addAllAttributes(map);
        return "item/index";
    }
}
