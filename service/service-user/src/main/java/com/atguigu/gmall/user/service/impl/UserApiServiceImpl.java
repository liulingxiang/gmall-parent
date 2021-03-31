package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserApiService;
import com.atguigu.gmall.util.MD5;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserApiServiceImpl implements UserApiService {

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {

        userInfo.setPasswd(MD5.encrypt(userInfo.getPasswd()));

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name",userInfo.getLoginName());
        queryWrapper.eq("passwd",userInfo.getPasswd());
        UserInfo userInfoLogin = userInfoMapper.selectOne(queryWrapper);

        if (userInfoLogin!=null){
            // 登录成功
            // 生成token
            String token = UUID.randomUUID().toString();
            // 放入redis
            redisTemplate.opsForValue().set("user:token:"+token,userInfoLogin.getId()+"");
            // 页面放入token
            userInfo.setToken(token);
            return userInfo;
        }else {
            // 登录失败
            return null;
        }
    }

    @Override
    public Map<String, Object> verify(String token) {
        Map<String,Object> map = new HashMap<>();
        String userIdFromCache = (String) redisTemplate.opsForValue().get("user:token:" + token);
        if (!StringUtils.isEmpty(userIdFromCache)){
            map.put("userId",userIdFromCache);
        }
        return map;
    }

    @Override
    public List<UserAddress> getUserAddresses(String userId) {

        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<UserAddress> addresses = userAddressMapper.selectList(queryWrapper);

        return addresses;
    }
}
