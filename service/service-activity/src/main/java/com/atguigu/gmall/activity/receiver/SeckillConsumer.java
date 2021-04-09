package com.atguigu.gmall.activity.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.activity.service.ActivityApiService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SeckillConsumer {
    @Autowired
    ActivityApiService activityApiService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = MqConst.ROUTING_SECKILL_USER,
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER)
    ))
    public void a(Channel channel, Message message, String recodeJson){

        //抢库存
        UserRecode userRecode = JSON.parseObject(recodeJson,UserRecode.class);
        String status = CacheHelper.get(userRecode.getSkuId() + "");
        if (status!=null&&status.equals("1")){
            activityApiService.seckillStock(userRecode);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
