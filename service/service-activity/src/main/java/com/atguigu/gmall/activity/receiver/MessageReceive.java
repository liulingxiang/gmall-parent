package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.util.CacheHelper;
import org.springframework.stereotype.Component;

@Component
public class MessageReceive {

    public void receiveMessage(String message) {

        if (message != null) {
            message = message.replaceAll("\"", "");
            String[] split = message.split(":");
            if (split != null && split.length > 0) {
                String sku_id = split[0];
                String status = split[1];
                // 将状态存储到jvm的静态的常量中，方便后面在抢购程序随时获取商品的抢购状态
                CacheHelper.put(sku_id, status);
            }
        }
        System.out.println(1);
    }
}
