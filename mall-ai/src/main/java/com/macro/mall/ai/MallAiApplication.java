package com.macro.mall.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.macro.mall")
public class MallAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAiApplication.class, args);
    }
}
