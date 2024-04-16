package com.springboot.architectural.repository;

import com.springboot.architectural.entity.PaymentRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisRepository {

//    public static final String HASH_KEY = "Product";
    @Autowired
    private RedisTemplate template;
    @CachePut
    public PaymentRedis save(PaymentRedis paymentRedis){
        template.opsForHash().put(paymentRedis.getCode(), paymentRedis.getUsername(),paymentRedis);
        System.out.println(paymentRedis);
        template.expire(paymentRedis.getCode(), 15, TimeUnit.MINUTES);
        return paymentRedis;
    }

    public PaymentRedis findPaymentRedisById(String code, String username){
        return (PaymentRedis) template.opsForHash().get(code,username);
    }

}
