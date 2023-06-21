package com.example.kursovaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KursovayaApplication {
    public static void main(String[] args) {
        SpringApplication.run(KursovayaApplication.class, args);
    }
}
