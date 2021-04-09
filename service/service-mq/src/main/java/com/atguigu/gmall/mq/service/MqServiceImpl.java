package com.atguigu.gmall.mq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqServiceImpl implements MqService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void testSendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }

    @Override
    public void testSendDLQ(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message,messagePostProcessor->{
            //重新设置信息的执行参数
            messagePostProcessor.getMessageProperties().setExpiration(10*1000+"");//设置过期时间
            return messagePostProcessor;
        });
    }

    @Override
    public void testSendDelay(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message,messagePostProcessor -> {
            //重新设置信息的执行参数
            messagePostProcessor.getMessageProperties().setDelay(20*1000);//设置延迟时间
            return messagePostProcessor;
        });
    }
}
