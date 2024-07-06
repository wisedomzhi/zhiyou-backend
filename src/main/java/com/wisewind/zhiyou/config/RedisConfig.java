package com.wisewind.zhiyou.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Setting the default serializer
        log.info("set key serializer");
        template.setKeySerializer(new StringRedisSerializer());
        log.info("set value serializer");
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Optionally, set hash key and value serializers
        log.info("set hash key serializer");
        template.setHashKeySerializer(new StringRedisSerializer());
        log.info("set hash value serializer");
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
