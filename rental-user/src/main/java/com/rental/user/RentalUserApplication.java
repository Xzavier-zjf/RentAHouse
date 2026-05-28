package com.rental.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.rental.user.mapper")
@SpringBootApplication(scanBasePackages = {
		"com.rental.user",
		"com.example.rentalcommon"
})
public class RentalUserApplication {
	public static void main(String[] args) {
		SpringApplication.run(RentalUserApplication.class, args);
	}
}
