package com.schoolmanagement.auth;

import com.schoolmanagement.common.GlobalExceptionHandler;
import com.schoolmanagement.security.ServiceSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GlobalExceptionHandler.class, ServiceSecurityConfiguration.class})
public class AuthApplication {
    public static void main(String[] args) { SpringApplication.run(AuthApplication.class, args); }
}
