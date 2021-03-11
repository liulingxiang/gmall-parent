package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product/")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;
    @RequestMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuService.saveSkuInfo(skuInfo);
        return Result.ok();
    }
    @RequestMapping("list/{page}/{limit}")
    public Result getSkuList(@PathVariable("page") Long page,
                             @PathVariable("limit") Long limit){
        Page<SkuInfo> skuInfoPage = skuService.getSkuList(page,limit);
        return Result.ok(skuInfoPage);
    }
    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){
        skuService.onSale(skuId);
        return Result.ok();
    }
    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){
        skuService.cancelSale(skuId);
        return Result.ok();
    }
}
