package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.atguigu.gmall.mq.service.MqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqController {

    @Autowired
    MqService mqService;

    @RequestMapping("testSendMessage")
    public String testSendMessage(){
        String exchange = "testExchange";
        String routingKey = "testRouting";
        String message = "1";
        mqService.testSendMessage(exchange,routingKey,message);
        return "nice!";
    }

    @RequestMapping("testSendDLQ")
    public String testSendDLQ(){
        String exchange = DeadLetterMqConfig.exchange_dead;
        String routingKey = DeadLetterMqConfig.routing_dead_1;
        String message = "test DLQ";
        mqService.testSendDLQ(exchange,routingKey,message);
        return "nice!";
    }

    @RequestMapping("testSendDelay")
    public String testSendDelay(){
        String exchange = DelayedMqConfig.exchange_delay;
        String routingKey = DelayedMqConfig.routing_delay;
        String message = "test Delay";
        mqService.testSendDelay(exchange,routingKey,message);
        return "nice!";
    }
}
