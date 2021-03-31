package com.atguigu.gmall.list.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.util.List;

public interface ListApiService {
    void createGoods(Class<Goods> goodsClass);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    List<JSONObject> getBaseCategoryList();

    SearchResponseVo search(SearchParam searchParam);

    void hotScore(Long skuId);
}
