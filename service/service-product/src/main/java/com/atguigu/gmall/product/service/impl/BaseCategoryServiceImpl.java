package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.BaseCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseCategoryServiceImpl implements BaseCategoryService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("category1_id",category1Id);
        return baseCategory2Mapper.selectList(wrapper2);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> wrapper3 = new QueryWrapper<>();
        wrapper3.eq("category2_id",category2Id);
        return baseCategory3Mapper.selectList(wrapper3);
    }

    @GmallCache
    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {

        QueryWrapper<BaseCategoryView> baseCategoryViewQueryWrapper = new QueryWrapper<>();
        baseCategoryViewQueryWrapper.eq("category3_id",category3Id);
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectOne(baseCategoryViewQueryWrapper);
        return baseCategoryView;
    }

    @Override
    public List<BaseCategoryView> getBaseCategoryList() {
        return baseCategoryViewMapper.selectList(null);
    }

}
