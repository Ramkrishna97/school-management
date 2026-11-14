package com.schoolmanagement.inventory;

import com.schoolmanagement.common.GlobalExceptionHandler;
import com.schoolmanagement.security.ServiceSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import({GlobalExceptionHandler.class, ServiceSecurityConfiguration.class})
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
