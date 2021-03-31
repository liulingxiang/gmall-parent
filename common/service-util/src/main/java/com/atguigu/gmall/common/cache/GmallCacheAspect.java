package com.atguigu.gmall.common.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object a(ProceedingJoinPoint point) {

        Object proceed = null;
        // 代理方法
        System.out.println("前置内容");

        Object[] args = point.getArgs();

        String keyId = "";

        if(null!=args&&args.length>0){
            for (Object arg : args) {
                keyId= keyId + ":"+arg;
            }
        }
        MethodSignature methodSignature = (MethodSignature)point.getSignature();
        String name = methodSignature.getMethod().getName();
        GmallCache annotation = methodSignature.getMethod().getAnnotation(GmallCache.class);
        String suffix = "";
        if(name.toLowerCase().contains("sku")){
            suffix = annotation.getSkuSuffix();
        }
        String key = name+":"+keyId+suffix;

//        // 缓存查询
//        Class returnType = methodSignature.getReturnType();
//        // 通过反射获得当前方法的返回值类型，因为当前方法的返回值类型决定着redis中的存储方式
//        String typeName = returnType.getTypeName();
//        String typeSuperName = returnType.getSuperclass().getTypeName();

        proceed = getCache(key);

        if(null==proceed){
            // 获得分布式锁
            String lockKey = "sku:" + keyId + ":lock";

            String localValue = UUID.randomUUID().toString();
            Boolean OK = redisTemplate.opsForValue().setIfAbsent(lockKey, localValue, 3, TimeUnit.SECONDS);// 三秒后自动释放锁

            if(OK){
                try {
                    proceed = point.proceed();// 执行被代理方法,db查询
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                if(null==proceed){
                    proceed = new Object();
                }
                // 同步缓存
                redisTemplate.opsForValue().set(key,proceed);
            }else {
                // 自旋嘛？
                // return a(point);
                // 又查了一次缓存
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getCache(key);// 自旋
            }

            // 删除分布式锁
            // 使用lua脚本对锁进行删除
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";// 脚本字符串
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();// 封装redis的lua脚本对象
            redisScript.setResultType(Long.class);// 脚本的返回值类型
            redisScript.setScriptText(script);
            redisTemplate.execute(redisScript, Arrays.asList(lockKey), localValue);
        }

        System.out.println("后置内容");
        return proceed;
    }

    private Object getCache(String key) {
        Object proceed;
        proceed = redisTemplate.opsForValue().get(key);
        return proceed;
    }
}
