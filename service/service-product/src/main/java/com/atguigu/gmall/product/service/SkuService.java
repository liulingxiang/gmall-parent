package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SkuService {

    void saveSkuInfo(SkuInfo skuInfo);

    Page<SkuInfo> getSkuList(Long page, Long limit);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SkuInfo getSkuInfo(Long skuId);

    List<SkuImage> getSkuInfoImageList(Long skuId);

    BigDecimal getSkuPrice(Long skuId);

    List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId);
}
