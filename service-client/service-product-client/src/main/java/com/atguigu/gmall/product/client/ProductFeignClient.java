package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(value = "service-product")
public interface ProductFeignClient {

    @RequestMapping("api/product/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getCategoryViewByCategory3Id/{category3Id}")
    BaseCategoryView getCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id);

    @RequestMapping("api/product/getSkuInfoImageList/{skuId}")
    List<SkuImage> getSkuInfoImageList(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("spuId") Long spuId, @PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSaleAttrValuesBySpu/{spuId}")
    List<Map<String, Object>> getSaleAttrValuesBySpu(@PathVariable("spuId")Long spuId);

    @RequestMapping("api/product/getTrademark/{tmId}")
    BaseTrademark getTrademark(@PathVariable("tmId") Long tmId);

    @RequestMapping("api/product/getSearchAttrs/{skuId}")
    List<SearchAttr> getSearchAttrs(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getBaseCategoryList")
    List<BaseCategoryView> getBaseCategoryList();
}
