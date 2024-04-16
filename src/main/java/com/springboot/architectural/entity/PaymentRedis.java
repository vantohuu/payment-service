package com.springboot.architectural.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("ConsentOtpRedis")
public class PaymentRedis implements Serializable {
    private String username;
    private String email;
    private String movieId;
    private String movieName;
    @Id
    private String code;
//    private LocalDateTime timestamp;

}