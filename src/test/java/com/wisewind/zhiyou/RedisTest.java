package com.wisewind.zhiyou;
import java.util.Date;

import com.wisewind.zhiyou.model.domain.User;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class RedisTest {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Test
    public void testRedis(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("testInt",1);
        valueOperations.set("testString", "test");
        User user = new User();
        user.setId(0L);
        user.setUsername("test");
        valueOperations.set("user",  user);

        int testInt = (int)valueOperations.get("testInt");
        assertEquals(testInt, 1);
        String testString = (String) valueOperations.get("testString");
        assertEquals(testString, "test");
        user = (User)valueOperations.get("user");
        System.out.println(user);

    }
}
