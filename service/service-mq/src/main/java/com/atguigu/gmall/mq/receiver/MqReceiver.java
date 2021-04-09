package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MqReceiver {

    //普通队列
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "testExchange",autoDelete = "true"),
            key = "testRouting",
            value = @Queue(value = "testQueue",durable = "true")
    ))
    public void a(String Object, Message message, Channel channel){
        //消费代码
        System.out.println(Object);
        System.out.println(message.getBody().toString());
        //手动确认接受信息
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag,false);
    }

    //死信队列
    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void b(String object,Message message,Channel channel){
        //消费代码
        System.out.println(object);
        System.out.println(message);

        //手动确认接收信息
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag,false);
    }

    //延迟队列
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void c(String object,Message message,Channel channel){
        //消费代码
        System.out.println(object);
        System.out.println(message);

        //手动确认接收信息
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag,false);
    }
}
