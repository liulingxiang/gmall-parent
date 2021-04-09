package com.atguigu.gmall.mq.confirm;

import io.micrometer.core.lang.Nullable;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MQPublisherAckConfirm implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void MQPublisherAckConfirm() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void confirm(@Nullable CorrelationData correlationData, boolean b, @Nullable String message) {
        System.out.println(b);
    }

    @Override
    public void returnedMessage(Message message, int deliverCode, String errMessage, String exchange, String routingKey) {
        System.out.println(deliverCode);
        System.out.println(errMessage);
        System.out.println(exchange);
        System.out.println(routingKey);
    }
}
