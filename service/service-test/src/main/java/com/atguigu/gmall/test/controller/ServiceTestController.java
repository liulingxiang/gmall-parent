package com.atguigu.gmall.test.controller;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.test.service.BaseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/product/")
@CrossOrigin
public class ServiceTestController {

    @Autowired
    BaseCategoryService baseCategoryService;

    @RequestMapping("getCategory1")
    public Result getCategory1() {
        List<BaseCategory1> BaseCategoryList = new ArrayList<>();
        BaseCategoryList = baseCategoryService.getCategory1();
        return Result.ok(BaseCategoryList);
    }

    @RequestMapping("test")
    public Result test(){

        return Result.ok("11111111111");
    }
}
