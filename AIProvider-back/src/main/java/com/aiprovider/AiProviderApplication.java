package com.aiprovider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aiprovider.mapper")
public class AiProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiProviderApplication.class, args);
    }
}