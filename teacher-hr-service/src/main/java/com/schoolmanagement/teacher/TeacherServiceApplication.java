package com.schoolmanagement.teacher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import com.schoolmanagement.common.GlobalExceptionHandler;
import com.schoolmanagement.security.ServiceSecurityConfiguration;

@SpringBootApplication
@EnableFeignClients
@Import({GlobalExceptionHandler.class, ServiceSecurityConfiguration.class})
public class TeacherServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeacherServiceApplication.class, args);
    }
}
