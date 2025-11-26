package com.staff.employee.dto;

import lombok.Builder;

@Builder
public record EmployeeFilter(
        String nameContains,
        Integer minAge,
        Integer maxAge,
        String department
) {
}
