package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;

import java.util.List;

public interface BaseCategoryService {

    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    List<BaseCategoryView> getBaseCategoryList();
}
