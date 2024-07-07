package com.wisewind.zhiyou.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String port;

    private String host;

    private String password;

    private Integer database;
    @Bean
    public RedissonClient redissonClient(){
        // 1. Create config object
        Config config = new Config();
        String address = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(address).setDatabase(database).setPassword(password);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
