package com.staff.employee.dto;

import com.staff.employee.model.Employee;
import lombok.Builder;

import java.util.List;

@Builder
public record EmployeePage(
        List<Employee> content,
        long totalElements,
        int totalPages
) {
}
