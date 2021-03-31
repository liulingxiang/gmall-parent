package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {

    @Autowired
    ListFeignClient listFeignClient;

    @RequestMapping({"list.html", "search.html"})
    public String search(SearchParam searchParam, Model model) {
        SearchResponseVo searchResponseVo = listFeignClient.search(searchParam);
        model.addAttribute("goodsList", searchResponseVo.getGoodsList());
        model.addAttribute("trademarkList", searchResponseVo.getTrademarkList());
        model.addAttribute("attrsList", searchResponseVo.getAttrsList());
        model.addAttribute("urlParam", getUrlParam(searchParam));
        model.addAttribute("searchParam", searchParam);

        // 页面功能参数(面包屑)
        if (StringUtils.isNotEmpty(searchParam.getTrademark())) {
            String trademark = searchParam.getTrademark();
            String[] split = trademark.split(":");
            String tmName = split[1];
            model.addAttribute("trademarkParam", tmName);
        }
        if (searchParam.getProps() != null && searchParam.getProps().length > 0) {
            List<SearchAttr> searchAttrList = new ArrayList<>();
            for (String prop : searchParam.getProps()) {
                String[] split = prop.split(":");
                Long attrId = Long.valueOf(split[0]);
                String attrValue = split[1];
                String attrName = split[2];
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(attrId);
                searchAttr.setAttrName(attrName);
                searchAttr.setAttrValue(attrValue);
                searchAttrList.add(searchAttr);
            }
            model.addAttribute("propsParamList", searchAttrList);
        }
        if (StringUtils.isNotEmpty(searchParam.getOrder())) {
            Map<String, String> orderMap = new HashMap<>();
            String[] split = searchParam.getOrder().split(":");
            orderMap.put("type", split[0]);
            orderMap.put("sort", split[1]);
            model.addAttribute("orderMap", orderMap);
        }

        return "list/index";
    }

    private Object getUrlParam(SearchParam searchParam) {

        String urlParam = "list.html?";
        Long category3Id = searchParam.getCategory3Id();// 三级分类id
        String order = searchParam.getOrder();// 排序
        String[] props = searchParam.getProps();// 属性
        String trademark = searchParam.getTrademark();// 商标
        String keyword = searchParam.getKeyword();// 关键字

        if (null != category3Id) {
            urlParam = urlParam + "category3Id=" + category3Id;
        }
        if (StringUtils.isNotEmpty(keyword)) {
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (StringUtils.isNotEmpty(trademark)) {
            urlParam = urlParam + "&trademark=" + trademark;
        }
        if (null != props && props.length > 0) {
            for (String prop : props) {
                urlParam = urlParam + "&props=" + prop;//props=1&props=2&props=3
            }
        }

        return urlParam;
    }

}
