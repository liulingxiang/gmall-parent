package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseTrademarkServiceImpl implements BaseTrademarkService {

    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;
    @Override
    public Page<BaseTrademark> baseTrademark(Long page, Long limit) {
        Page<BaseTrademark> baseTrademarkPage = new Page<>();
        baseTrademarkPage.setCurrent(page);
        baseTrademarkPage.setSize(limit);
        baseTrademarkMapper.selectPage(baseTrademarkPage, null);
        return baseTrademarkPage;
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {
        List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null);
        return baseTrademarkList;
    }
}
