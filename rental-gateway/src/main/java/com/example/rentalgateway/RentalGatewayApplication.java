package com.example.rentalgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.example.rentalgateway",
		"com.example.rentalcommon"
})
public class RentalGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(RentalGatewayApplication.class, args);
	}
}
