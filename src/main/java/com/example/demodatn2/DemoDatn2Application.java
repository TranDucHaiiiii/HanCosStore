package com.example.demodatn2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.example.demodatn2")
public class DemoDatn2Application {

    public static void main(String[] args) {
        SpringApplication.run(DemoDatn2Application.class, args);
    }

}
