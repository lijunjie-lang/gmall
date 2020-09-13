package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;



    @Around("@annotation(GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        GmallCache annotation = method.getAnnotation(GmallCache.class);

        Class<?> returnType = method.getReturnType();

        String prefix = annotation.prefix();
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        String key = prefix + args;
        String json = this.redisTemplate.opsForValue().get(key);

        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,returnType);
        }

        String lock = annotation.lock();
        RLock fairLock = this.redissonClient.getFairLock(lock + ":" + args);
        fairLock.lock();

        String json2 = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json2)){
            fairLock.unlock();
            return JSON.parseObject(json2,returnType);
        }

        Object result = joinPoint.proceed(joinPoint.getArgs());

        int random = annotation.random();
        int timeout = annotation.timeout() + new Random().nextInt(random);
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result),timeout, TimeUnit.MINUTES);

        fairLock.unlock();

        return result;

    }




}
