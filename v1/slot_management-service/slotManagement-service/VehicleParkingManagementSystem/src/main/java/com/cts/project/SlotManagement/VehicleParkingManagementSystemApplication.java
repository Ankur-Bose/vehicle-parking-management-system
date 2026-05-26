package com.cts.project.SlotManagement;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VehicleParkingManagementSystemApplication implements CommandLineRunner {


	public static void main(String[] args) {
		SpringApplication.run(VehicleParkingManagementSystemApplication.class, args);

        System.out.println("helelel");


	}

    @Override
    public void run(String... args) throws Exception {

//        parkingSlotService.getAllSlots();
//        parkingSlotService.getSlotById(22L);

//        parkingSlotService.getSlotsByType("2W");
//        parkingSlotService.getSlotsByType("4W");
//
//
//        ParkingSlot s2 = new ParkingSlot();
//        s2.setType("2W");
//        s2.setLocation("A");
//        s2.setIsOccupied(1);
//        s2.setId(0);
//
//        parkingSlotService.saveSlot(s2);
//
//        ParkingSlot s3 = new ParkingSlot();
//        s3.setType("2W");
//        s3.setLocation("B");
//        s3.setIsOccupied(-1);

//        parkingSlotService.saveSlot(s3);



    }
}
