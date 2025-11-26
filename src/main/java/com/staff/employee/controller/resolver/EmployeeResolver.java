package com.staff.employee.controller.resolver;

import com.staff.employee.dto.EmployeeFilter;
import com.staff.employee.dto.EmployeeInput;
import com.staff.employee.dto.EmployeePage;
import com.staff.employee.model.Employee;
import com.staff.employee.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@Slf4j
@Validated
public class EmployeeResolver {

    private final EmployeeService service;

    public EmployeeResolver(EmployeeService service) {
        this.service = service;
    }

    // Query: listEmployees
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public EmployeePage listEmployees(EmployeeFilter filter, Integer page, Integer size, String sort) {
        int p = page == null ? 0 : page;
        int s = size == null ? 10 : size;
        return service.listEmployees(filter, p, s, sort);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public CompletableFuture<Employee> employee(Long id, DataLoader<Long, Employee> employeeDataLoader) {
        // Use DataLoader for batching
        return employeeDataLoader.load(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Employee addEmployee(@RequestBody EmployeeInput input) {
        return service.createEmployee(input);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Employee updateEmployee(Long id, @RequestBody EmployeeInput input) {
        return service.updateEmployee(id, input);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteEmployee(Long id) {
        return service.deleteEmployee(id);
    }
}
