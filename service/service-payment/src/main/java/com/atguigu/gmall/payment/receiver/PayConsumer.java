
package com.atguigu.gmall.payment.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.payment.service.PayApiService;
import com.atguigu.gmall.rabbit.config.DelayedMqConfig;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PayConsumer {

    @Autowired
    RabbitService rabbitService;
    @Autowired
    PayApiService payApiService;

    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void a(String MessageStr, Message message, Channel channel) {

        Map<String, Object> map = new HashMap<>();
        map = JSON.parseObject(MessageStr, map.getClass());
        //获得检查次数
        Integer count = (Integer) map.get("count");
        //获取外部订单号
        String out_trade_no = (String) map.get("out_trade_no");
        //查询支付状态
        Map<String, Object> statusMap = payApiService.checkPayStatus(out_trade_no);
        boolean flag = (boolean) statusMap.get("success");
        if (flag) {
            //交易已创建
            String tradeStatus = (String) statusMap.get("tradeStatus");
            if (!tradeStatus.equals("WAIT_BUYER_PAY")) {
                //交易已完成，进行后续操作
                //更新支付状态
                //签收
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
        }
        //交易未创建
        System.out.println("延迟检查支付情况");

        //根据查询结果，循环下一次任务时间
        if (count > 0) {
            count--;
            map.put("count", count);
            rabbitService.sendDelay(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, JSON.toJSONString(map), 10);
        } else {
            System.out.println("检查次数上限，不检查支付结果");
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
