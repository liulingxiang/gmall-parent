package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TestController {
    @RequestMapping("test")
    public String test(Model model) {
        String str = "hello thymeleaf !";

        List<String> list = new ArrayList<>();

        for (int i = 0; i <5 ; i++) {
            list.add("元素:"+i);
        }

        model.addAttribute("list",list);

        model.addAttribute("str",str);

        model.addAttribute("num","迪迦奥特曼");
        return "test";
    }
}
