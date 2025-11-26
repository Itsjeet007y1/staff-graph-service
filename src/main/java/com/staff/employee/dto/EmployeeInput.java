package com.staff.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record EmployeeInput(
        @NotBlank String name,
        @NotNull Integer age,
        String department,
        List<String> skills,
        Integer attendance
) {
}
