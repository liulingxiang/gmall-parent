package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.product.service.BaseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id) {
        List<BaseCategory3> BaseCategoryList = new ArrayList<>();
        BaseCategoryList = baseCategoryService.getCategory3(category2Id);
        return Result.ok(BaseCategoryList);
    }
    @RequestMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id) {
        List<BaseCategory2> BaseCategoryList = new ArrayList<>();
        BaseCategoryList = baseCategoryService.getCategory2(category1Id);
        return Result.ok(BaseCategoryList);
    }
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
