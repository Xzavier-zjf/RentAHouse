package com.example.rentalmessage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.rentalmessage.mapper")
@SpringBootApplication(scanBasePackages = {
        "com.example.rentalmessage",
        "com.example.rentalcommon"
})
public class RentalMessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentalMessageApplication.class, args);
    }
}
