package com.atguigu.gmall.test.service.impl;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.test.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.test.service.BaseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseCategoryServiceImpl implements BaseCategoryService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Override
    public List<BaseCategory1> getCategory1() {

        return baseCategory1Mapper.selectList(null);
    }
}
