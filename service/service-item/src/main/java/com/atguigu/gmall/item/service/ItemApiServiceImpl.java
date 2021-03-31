package com.atguigu.gmall.item.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemApiServiceImpl implements ItemApiService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> item(Long skuId) {

        long A = System.currentTimeMillis();
        // 调用productFeignClient的基础服务
        HashMap<String, Object> map = new HashMap<>();

        CompletableFuture<SkuInfo> completableFutureSkuInfo = CompletableFuture.supplyAsync(() -> {
            // skuInfo
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            map.put("skuInfo", skuInfo);
            System.out.println(Thread.currentThread().getName());
            return skuInfo;
        }, threadPoolExecutor);

        // 分类
        CompletableFuture<Void> completableFutureCategory = completableFutureSkuInfo.thenAcceptAsync((skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id());
            map.put("categoryView", categoryView);
            System.out.println(Thread.currentThread().getName());

        }), threadPoolExecutor);
        // 图片
        CompletableFuture<Void> completableFutureImages = completableFutureSkuInfo.thenAcceptAsync((skuInfo -> {
            List<SkuImage> skuImages = productFeignClient.getSkuInfoImageList(skuId);
            skuInfo.setSkuImageList(skuImages);
            System.out.println(Thread.currentThread().getName());

        }), threadPoolExecutor);

        // 价格
        CompletableFuture<Void> completableFuturePrice = CompletableFuture.runAsync(() -> {
            BigDecimal price = productFeignClient.getSkuPrice(skuId);
            map.put("price", price);
            System.out.println(Thread.currentThread().getName());

        }, threadPoolExecutor);

        // 销售属性
        CompletableFuture<Void> completableFutureSpuSaleAttr = completableFutureSkuInfo.thenAcceptAsync((skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getSpuId(), skuId);
            map.put("spuSaleAttrList", spuSaleAttrList);
            System.out.println(Thread.currentThread().getName());

        }), threadPoolExecutor);
        // saleAttrHash，页面的spu的销售属性sku对应关系表
        CompletableFuture<Void> completableFutureSkuJsonMap = completableFutureSkuInfo.thenAcceptAsync((skuInfo -> {
            List<Map<String, Object>> valuesSkuJsonMapList = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());
            // List<map> - > map
            HashMap<String, Object> valuesSkuJsonMap = new HashMap<>();
            for (Map<String, Object> stringObjectMap : valuesSkuJsonMapList) {
                String valueIds = (String) stringObjectMap.get("valueIds");
                Integer sku_id = (Integer) stringObjectMap.get("sku_id");
                valuesSkuJsonMap.put(valueIds, sku_id);
            }
            String jsonString = JSON.toJSONString(valuesSkuJsonMap);
            // 封装前台页面的数据
            map.put("valuesSkuJson", jsonString);
            System.out.println(Thread.currentThread().getName());

        }), threadPoolExecutor);
        CompletableFuture.allOf(completableFutureSkuInfo, completableFutureCategory, completableFutureImages, completableFuturePrice, completableFutureSpuSaleAttr, completableFutureSkuJsonMap).join();

        long B = System.currentTimeMillis();
        System.out.println("结束时间：" + (B - A));

        //热度值
        listFeignClient.hotScore(skuId);

        return map;
    }
}
