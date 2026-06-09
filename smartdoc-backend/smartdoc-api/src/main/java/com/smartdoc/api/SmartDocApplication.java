package com.smartdoc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.smartdoc")
@EnableScheduling
public class SmartDocApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartDocApplication.class, args);
        System.out.println("(♥◠‿◠)ノ゙  SmartDoc 启动成功   ლ(´ڡ`ლ)゙");
    }
}
