package com.fintech.leanbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LeanBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeanBackendApplication.class, args);
    }

}
