package com.wisewind.zhiyou.once;
import java.util.Date;

import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
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
}
