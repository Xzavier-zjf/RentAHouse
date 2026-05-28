package com.example.rentalorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"com.example.rentalorder",
		"com.example.rentalcommon"
})
@EnableFeignClients(basePackages = "com.example.rentalorder.client")
public class RentalOrderApplication {
	public static void main(String[] args) {
		SpringApplication.run(RentalOrderApplication.class, args);
	}
}
