package com.atguigu.gmall.all;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients("com.atguigu.gmall")
@ComponentScan("com.atguigu.gmall")
@EnableDiscoveryClient
public class ServiceAllApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAllApplication.class,args);
    }
}
