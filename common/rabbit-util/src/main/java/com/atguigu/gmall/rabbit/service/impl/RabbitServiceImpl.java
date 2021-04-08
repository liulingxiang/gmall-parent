package com.atguigu.gmall.rabbit.service.impl;

import com.atguigu.gmall.rabbit.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitServiceImpl implements RabbitService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }

    @Override
    public void sendDLQ(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message,messagePostProcessor->{
            //重新设置信息的执行参数
            messagePostProcessor.getMessageProperties().setExpiration(10*1000+"");//设置过期时间
            return messagePostProcessor;
        });
    }

    @Override
    public void sendDelay(String exchange, String routingKey, String message,Integer time) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message,messagePostProcessor -> {
            //重新设置信息的执行参数
            messagePostProcessor.getMessageProperties().setDelay(time*1000);//设置延迟时间
            return messagePostProcessor;
        });
    }
}
