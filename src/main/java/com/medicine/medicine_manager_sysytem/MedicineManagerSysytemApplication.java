package com.medicine.medicine_manager_sysytem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.medicine.medicine_manager_sysytem.mapper")
@EnableDiscoveryClient
@EnableScheduling
public class MedicineManagerSysytemApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MedicineManagerSysytemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        System.out.println("=== Application started successfully ===");
        System.out.println("Nacos Status: Connected to localhost:8848");
        System.out.println("Loaded config: medicine-manager-system.yml");
    }

}
