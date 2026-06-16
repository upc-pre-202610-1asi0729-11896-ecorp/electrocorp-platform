package com.electrocorp.electrocorpplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ElectrocorpPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectrocorpPlatformApplication.class, args);
    }
}