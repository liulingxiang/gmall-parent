package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.BaseAttrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseAttrServiceImpl implements BaseAttrService {
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        QueryWrapper<BaseAttrInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category_level",3);
        wrapper.eq("category_id",category3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectList(wrapper);
        for (BaseAttrInfo baseAttrInfo : baseAttrInfoList) {
            QueryWrapper<BaseAttrValue> wrapper1 = new QueryWrapper<>();
            Long attrInfoId = baseAttrInfo.getId();
            wrapper1.eq("attr_id",attrInfoId);
            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(wrapper1);
            baseAttrInfo.setAttrValueList(baseAttrValueList);
        }
        return baseAttrInfoList;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        Long attrInfoId = baseAttrInfo.getId();
        if (attrInfoId==null||attrInfoId<=0){
            baseAttrInfoMapper.insert(baseAttrInfo);
        }else {
            baseAttrInfoMapper.updateById(baseAttrInfo);
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id",attrInfoId);
            baseAttrValueMapper.delete(wrapper);
        }
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(baseAttrValue);
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id",attrId);
        List<BaseAttrValue> valueList = baseAttrValueMapper.selectList(wrapper);
        return valueList;
    }
}
