package com.invoicely.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
	// Ek simple GET API banai hai check karne ke liye ki server zinda hai ya nahi.
	@GetMapping("/api/health")
	public String healthCheck(){
		return "Invoicely Backend is Up and Running";
	}
}


