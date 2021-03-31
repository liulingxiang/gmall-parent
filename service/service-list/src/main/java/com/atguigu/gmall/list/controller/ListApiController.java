package com.atguigu.gmall.list.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.ListApiService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    ListApiService listApiService;

    @RequestMapping("hotScore/{skuId}")
    void hotScore(@PathVariable("skuId")Long skuId){
        listApiService.hotScore(skuId);
    }

    @RequestMapping("search")
    SearchResponseVo search(@RequestBody SearchParam searchParam) {
        return listApiService.search(searchParam);
    }

    @RequestMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList() {
        return listApiService.getBaseCategoryList();
    }

    @RequestMapping("createGoods")
    public Result createGoods() {
        listApiService.createGoods(Goods.class);
        return Result.ok();
    }

    @RequestMapping("onSale/{skuId}")
    public void onSale(@PathVariable("skuId") Long skuId) {
        listApiService.onSale(skuId);
        System.out.println("商品上架");
    }

    @RequestMapping("cancelSale/{skuId}")
    public void cancelSale(@PathVariable("skuId") Long skuId) {
        listApiService.cancelSale(skuId);
        System.out.println("商品下架");
    }
}
