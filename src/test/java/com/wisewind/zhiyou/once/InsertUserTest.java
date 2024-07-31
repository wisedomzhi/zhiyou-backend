package com.wisewind.zhiyou.once;
import java.util.*;

import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.service.UserService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InsertUserTest {
    @Autowired
    public UserService userService;

    @Test
    public void testConcurrentInsert(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("fakeUser");
                user.setUserAccount("fakeUser123");
                user.setUserPassword("12346578");
                user.setAvatarUrl("https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg");
                user.setGender(0);
                user.setPhone("123456789011");
                user.setEmail("123132@qq.com");
                user.setUserStatus(0);
                user.setIsDelete(0);
                user.setUserRole(0);
                user.setUserProfile("test user");
                userList.add(user);
                if(j % 10000 == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(userList);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 测试优先级队列的使用
     */
    @Test
    public void tempTest(){
        PriorityQueue<Pair<User, Integer>> priorityQueue = new PriorityQueue<>(new Comparator<Pair<User, Integer>>() {
            @Override
            public int compare(Pair<User, Integer> o1, Pair<User, Integer> o2) {
                return o1.getValue() - o2.getValue();
            }
        });
        User user1 = new User();
        Pair<User, Integer> pair1 = new ImmutablePair<User, Integer>(user1, 1);
        Pair<User, Integer> pair2 = new ImmutablePair<User, Integer>(user1, 2);
        Pair<User, Integer> pair3 = new ImmutablePair<User, Integer>(user1, 3);

        priorityQueue.offer(pair3);
        priorityQueue.offer(pair2);
        priorityQueue.offer(pair1);

        while (!priorityQueue.isEmpty()){
            Pair<User, Integer> pair = priorityQueue.poll();
            System.out.println(pair.getValue());
        }
    }
}
