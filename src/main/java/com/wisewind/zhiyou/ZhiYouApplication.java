package com.wisewind.zhiyou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.wisewind.zhiyou.mapper")
@EnableScheduling
public class ZhiYouApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiYouApplication.class, args);
    }

}
