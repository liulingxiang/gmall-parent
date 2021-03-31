package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/product/")
public class ProductApiController {

    @Autowired
    private SkuService skuService;
    @Autowired
    private BaseCategoryService baseCategoryService;
    @Autowired
    private SpuService spuService;
    @Autowired
    BaseTrademarkService baseTrademarkService;
    @Autowired
    BaseAttrService baseAttrService;

    @RequestMapping("getBaseCategoryList")
    List<BaseCategoryView> getBaseCategoryList(){
        List<BaseCategoryView> baseCategoryViews = baseCategoryService.getBaseCategoryList();
        return baseCategoryViews;
    }

    @RequestMapping("getSearchAttrs/{skuId}")
    List<SearchAttr> getSearchAttrs(@PathVariable("skuId") Long skuId){
        List<SearchAttr> searchAttrs = baseAttrService.getSearchAttrs(skuId);
        return searchAttrs;
    }

    @RequestMapping("getTrademark/{tmId}")
    BaseTrademark getTrademark(@PathVariable("tmId") Long tmId){
        BaseTrademark trademark = baseTrademarkService.getTrademark(tmId);
        return trademark;
    }

    @RequestMapping("getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = skuService.getSkuInfo(skuId);
        return skuInfo;
    }
    @RequestMapping("getCategoryViewByCategory3Id/{category3Id}")
    BaseCategoryView getCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id){
        BaseCategoryView baseCategoryView = baseCategoryService.getCategoryViewByCategory3Id(category3Id);
        return baseCategoryView;
    }
    @RequestMapping("getSkuInfoImageList/{skuId}")
    List<SkuImage> getSkuInfoImageList(@PathVariable("skuId") Long skuId){
        List<SkuImage> imageList = skuService.getSkuInfoImageList(skuId);
        return imageList;
    }

    @RequestMapping("getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId){
        BigDecimal price = skuService.getSkuPrice(skuId);
        return price;
    }
    @RequestMapping("getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("spuId") Long spuId, @PathVariable("skuId") Long skuId){

        return spuService.getSpuSaleAttrListCheckBySku(spuId,skuId);
    }
    @RequestMapping("getSaleAttrValuesBySpu/{spuId}")
    List<Map<String, Object>> getSaleAttrValuesBySpu(@PathVariable("spuId")Long spuId){
        List<Map<String,Object>> mapList = skuService.getSaleAttrValuesBySpu(spuId);
        return mapList;
    }

}
