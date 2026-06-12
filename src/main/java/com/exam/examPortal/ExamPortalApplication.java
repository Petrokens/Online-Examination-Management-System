package com.exam.examPortal;

import com.exam.examPortal.entity.User;
import com.exam.examPortal.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExamPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamPortalApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserService userService) {
        return args -> {
            // Check if any admin exists using your service
            if (userService.getAdmins().isEmpty()) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail("admin@examportal.com");
                admin.setPassword("admin123");
                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");

                userService.registerUser(admin);
                System.out.println(">>> [AUTO-SETUP] Super Admin created: admin@examportal.com / admin123");
            }
        };
    }
}