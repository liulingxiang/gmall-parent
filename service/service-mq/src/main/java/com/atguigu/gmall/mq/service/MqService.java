package com.atguigu.gmall.mq.service;

public interface MqService {
    void testSendMessage(String exchange, String routingKey, String message);

    void testSendDLQ(String exchange, String routingKey, String message);

    void testSendDelay(String exchange, String routingKey, String message);
}
