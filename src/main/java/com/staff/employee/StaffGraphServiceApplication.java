package com.staff.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
public class StaffGraphServiceApplication {
    public static void main(String[] args) {
        // Use inheritable thread-local so child threads (GraphQL execution threads / DataLoader) inherit SecurityContext
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        SpringApplication.run(StaffGraphServiceApplication.class, args);
    }
}
