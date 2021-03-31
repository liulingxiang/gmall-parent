package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface BaseTrademarkService {
    Page<BaseTrademark> baseTrademark(Long page, Long limit);

    List<BaseTrademark> getTrademarkList();

    BaseTrademark getTrademark(Long tmId);
}
