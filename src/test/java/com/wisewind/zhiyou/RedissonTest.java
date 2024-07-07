package com.wisewind.zhiyou;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wisewind.zhiyou.model.domain.User;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
public class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedisson(){
        RList<String> redissonList = redissonClient.getList("redissonList");
        for (String s : redissonList) {
            System.out.println(s);
        }
    }

    @Test
    public void testRedissonLock(){
        RLock lock = redissonClient.getLock("zhiyou:user:recommend:lock");
        try {
            lock.tryLock(0, -1, TimeUnit.MILLISECONDS);
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
