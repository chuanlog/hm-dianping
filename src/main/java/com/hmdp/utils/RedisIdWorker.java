package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    private static final long BEGIN_TIMESTAMP = 1640995200L;//2022-01-01
    private static final int COUNT_BITS = 32;// 32位时间戳左移位数

    // 创建一个RedisIdWorker对象
    @Resource
    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) {
        // 1.生成时间戳
        long nowTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowTime - BEGIN_TIMESTAMP;
        // 2.生成序列号,这里使用Redis的自增ID，并使用date来提高唯一id的容量，而且方便未来的统计
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);//生成序列号
        // 3.拼接返回
        return timestamp << COUNT_BITS | count;
    }
}
