package com.wisewind.zhiyou.jobs;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RecommendCacheJob {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    private String[] vipsId = {"1"};

    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 0 0 * * ? ")
    public void cacheUser() {
        RLock lock = redissonClient.getLock("zhiyou:user:recommend:lock");
        try {
            lock.tryLock(0, -1, TimeUnit.MILLISECONDS);
            for (String id : vipsId) {
                String key = String.format("zhiyou:user:recommend:%s", id);
                Page<User> pageResult = userService.page(new Page<>(1, 4));
                List<User> userList = pageResult.getRecords();
                List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
                redisTemplate.opsForValue().set(key, list, 2, TimeUnit.HOURS);
            }
        } catch (InterruptedException e) {
           e.printStackTrace();
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }


    }
}
