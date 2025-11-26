package com.staff.employee.config;

import com.staff.employee.model.Employee;
import com.staff.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class SampleDataRunner {

    @Bean
    public CommandLineRunner loadData(EmployeeRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.saveAll(List.of(
                        Employee.builder().name("Alice").age(30).department("IT").skills(List.of("Java",".Net")).attendance(95).build(),
                        Employee.builder().name("Bob").age(25).department("Management").skills(List.of("HR","Hiring")).attendance(38).build(),
                        Employee.builder().name("Alex").age(21).department("Science").skills(List.of("R&D")).attendance(98).build(),
                        Employee.builder().name("Tery").age(45).department("IT").skills(List.of("Java")).attendance(83).build(),
                        Employee.builder().name("Charlie").age(28).department("DevOps").skills(List.of("CICD","Jenkins")).attendance(82).build()
                ));
                log.info("Sample employees created");
            }
        };
    }
}
