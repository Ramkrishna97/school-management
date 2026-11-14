package com.schoolmanagement.timetable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Import;
import com.schoolmanagement.common.GlobalExceptionHandler;
import com.schoolmanagement.security.ServiceSecurityConfiguration;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@ConfigurationPropertiesScan
@Import({GlobalExceptionHandler.class, ServiceSecurityConfiguration.class})
public class TimetableApplication {
    public static void main(String[] args) {
        SpringApplication.run(TimetableApplication.class, args);
    }
}
