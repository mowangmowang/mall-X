package com.macro.mall.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商城前台应用启动类
 * 负责启动用户端服务，处理商品浏览、下单、支付等业务
 */
@SpringBootApplication(scanBasePackages = "com.macro.mall")
public class MallPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPortalApplication.class, args);
    }

}
