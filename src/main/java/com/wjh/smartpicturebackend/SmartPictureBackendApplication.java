package com.wjh.smartpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.wjh.smartpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class SmartPictureBackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(SmartPictureBackendApplication.class, args);
    }

}
