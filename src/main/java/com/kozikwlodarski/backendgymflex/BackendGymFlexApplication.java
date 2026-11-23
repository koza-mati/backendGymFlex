package com.kozikwlodarski.backendgymflex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BackendGymFlexApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendGymFlexApplication.class, args);
    }

}
