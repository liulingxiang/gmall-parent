package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SkuService {

    void saveSkuInfo(SkuInfo skuInfo);

    Page<SkuInfo> getSkuList(Long page, Long limit);

    void onSale(Long skuId);

    void cancelSale(Long skuId);
}
