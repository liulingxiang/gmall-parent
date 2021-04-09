package com.atguigu.gmall.rabbit.service;

public interface RabbitService {
    void sendMessage(String exchange, String routingKey, String message);

    void sendDLQ(String exchange, String routingKey, String message);

    void sendDelay(String exchange, String routingKey, String message,Integer time);
}
