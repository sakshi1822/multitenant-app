package com.example.multitenantapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableScheduling
@SpringBootApplication
public class MultitenantappApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultitenantappApplication.class, args);
	}

}
