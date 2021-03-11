package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SpuService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/")
@CrossOrigin
public class SpuController {
    @Autowired
    private SpuService spuService;

    @RequestMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> saleAttrList = spuService.spuSaleAttrList(spuId);
        return Result.ok(saleAttrList);
    }

    @RequestMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId){
        List<SpuImage> imageList = spuService.spuImageList(spuId);
        return Result.ok(imageList);
    }

    @RequestMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    @RequestMapping("{page}/{limit}")
    public Result getSpuList(@PathVariable("page") Long page,
                             @PathVariable("limit") Long limit,
                             Long category3Id){
        Page<SpuInfo> spuInfoPage = spuService.getSpuList(page,limit,category3Id);
        return Result.ok(spuInfoPage);
    }
    @RequestMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = spuService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }
}
