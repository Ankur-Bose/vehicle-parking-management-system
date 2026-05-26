package com.cts.project.vpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//Fundamentals → Web (REST) → JPA → Security → JWT → Mail → Testing (need to learn these)

@SpringBootApplication
@EnableDiscoveryClient
public class UserRegistrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserRegistrationServiceApplication.class, args);
	}

}