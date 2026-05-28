package com.example.rentalhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.example.rentalhouse",
		"com.example.rentalcommon"
})
public class RentalHouseApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalHouseApplication.class, args);
	}

}
