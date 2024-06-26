package com.wisewind.zhiyou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wisewind.zhiyou.mapper")
public class ZhiYouApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiYouApplication.class, args);
    }

}
