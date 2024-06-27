package com.wisewind.zhiyou.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wisewind.zhiyou.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    void testSave(){

        User user = new User();
        user.setUsername("");
        user.setUserAccount("");
        user.setUserPassword("");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setPhone("");
        user.setEmail("");
        user.setUserStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);

        boolean result = userService.save(user);
        System.out.println(user.getId());
        assertTrue(result);
    }


    @Test
    void testReg(){
        String regEx = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(regEx).matcher("userAccount213_d");
        boolean res = matcher.find();
        assertEquals(true, res);
    }

    @Test
    void testGetByTags(){
        List<String> strings = new ArrayList<>();
        strings.add("java");
        strings.add("python");
        List<User> byTags = userService.getByTagsMemo(strings);

    }

    @Test
    void testGetById(){
        User user = userService.getById(1);
        assertNotNull(user);
    }

    @Test
    void testGson(){
        Gson gson = new Gson();
        List<String> tagList = new ArrayList<>();
        tagList.add("java");
        tagList.add("python");
        String s = gson.toJson(tagList);
        assertEquals(s, "[\"java\",\"python\"]");
    }
}
