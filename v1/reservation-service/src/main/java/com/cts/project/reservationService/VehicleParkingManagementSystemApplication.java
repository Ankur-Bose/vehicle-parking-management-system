package com.cts.project.reservationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class    VehicleParkingManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleParkingManagementSystemApplication.class, args);
	}

}
