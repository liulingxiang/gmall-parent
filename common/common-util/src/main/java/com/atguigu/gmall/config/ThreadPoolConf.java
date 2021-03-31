package com.atguigu.gmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConf {

    @Bean
    public ThreadPoolExecutor MyThreadPool(){
        return new ThreadPoolExecutor(50,500,10, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(10000));
    }

}
