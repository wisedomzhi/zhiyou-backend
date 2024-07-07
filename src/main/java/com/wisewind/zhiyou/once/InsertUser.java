package com.wisewind.zhiyou.once;

import com.wisewind.zhiyou.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InsertUser {


//    @Scheduled(fixedDelay = 5000)
    public void Insert(){
        System.out.println("ddd");
    }
}
