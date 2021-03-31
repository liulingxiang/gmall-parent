package com.atguigu.gmall.test.controller;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.locks.Lock;

@RestController
public class RedissonTest {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("/test")
    public String test(){
        Lock lock = redissonClient.getLock("num:lock");
        lock.lock();
        Integer num = 0;
        try {
            num = (Integer) redisTemplate.opsForValue().get("num");
            num--;
            redisTemplate.opsForValue().set("num",num);
            System.out.println("接收访问,剩余库存" + num);
        }finally {
            lock.unlock();
        }

        return num+"";
    }
}
