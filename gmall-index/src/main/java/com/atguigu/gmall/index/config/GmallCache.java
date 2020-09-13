package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存前缀
     * @return
     */
    String prefix() default "cache:";

    /**
     * 缓存时间：默认是60min
     * 单位：分钟
     * @return
     */
    int timeout() default 60;

    /**
     * 为了防止缓存雪崩，给缓存时间指定随机值
     * 单位：分钟
     * @return
     */
    int random() default 10;

    /**
     * 为了防止缓存击穿，给缓存指定分布式锁的名称
     * @return
     */
    String lock() default "lock";




}
