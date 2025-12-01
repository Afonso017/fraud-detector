package com.frauddetector.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileApplication.class, args);
    }
}
