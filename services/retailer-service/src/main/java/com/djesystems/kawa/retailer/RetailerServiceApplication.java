package com.djesystems.kawa.retailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RetailerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                RetailerServiceApplication.class,
                args
        );
    }
}