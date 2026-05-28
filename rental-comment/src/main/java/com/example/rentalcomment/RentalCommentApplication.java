package com.example.rentalcomment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.example.rentalcomment",
        "com.example.rentalcommon"
})
@EnableFeignClients(basePackages = "com.example.rentalcomment.client")
public class RentalCommentApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentalCommentApplication.class, args);
    }
}
