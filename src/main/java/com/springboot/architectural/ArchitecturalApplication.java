package com.springboot.architectural;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ArchitecturalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArchitecturalApplication.class, args);
	}
}
