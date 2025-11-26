package com.staff.employee.controller;

import com.staff.employee.controller.resolver.EmployeeResolver;
import com.staff.employee.dto.EmployeeFilter;
import com.staff.employee.dto.EmployeeInput;
import com.staff.employee.dto.EmployeePage;
import com.staff.employee.model.Employee;
import org.dataloader.DataLoader;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class GraphQlEmployeeController {

    private final EmployeeResolver resolver;

    public GraphQlEmployeeController(EmployeeResolver resolver) {
        this.resolver = resolver;
    }

    @QueryMapping
    public EmployeePage listEmployees(@Argument EmployeeFilter filter, @Argument Integer page, @Argument Integer size, @Argument String sort) {
        return resolver.listEmployees(filter, page, size, sort);
    }

    @QueryMapping
    public CompletableFuture<Employee> employee(@Argument Long id, @ContextValue(name = "dataLoaderRegistry") org.dataloader.DataLoaderRegistry registry) {
        DataLoader<Long, Employee> loader = registry.getDataLoader("employeeDataLoader");
        return resolver.employee(id, loader);
    }

    @MutationMapping
    public Employee addEmployee(@Argument EmployeeInput input) {
        return resolver.addEmployee(input);
    }

    @MutationMapping
    public Employee updateEmployee(@Argument Long id, @Argument EmployeeInput input) {
        return resolver.updateEmployee(id, input);
    }

    @MutationMapping
    public Boolean deleteEmployee(@Argument Long id) {
        return resolver.deleteEmployee(id);
    }
}
