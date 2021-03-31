package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemApiService itemApiService;

    @RequestMapping("{skuId}")
    Map<String, Object> item(@PathVariable("skuId") Long skuId){
        Map<String,Object> map = itemApiService.item(skuId);
        return map;
    }
}
