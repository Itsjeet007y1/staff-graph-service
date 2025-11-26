package com.staff.employee.service;

import com.staff.employee.dto.EmployeeFilter;
import com.staff.employee.dto.EmployeeInput;
import com.staff.employee.dto.EmployeePage;
import com.staff.employee.exception.NotFoundException;
import com.staff.employee.model.Employee;
import com.staff.employee.repository.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public EmployeePage listEmployees(EmployeeFilter filter, int page, int size, String sort) {
        Sort sortObj = Sort.by(Sort.Direction.ASC, "id");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            List<Sort.Order> orders = new ArrayList<>();
            for (String p : parts) {
                String field = p.trim();
                Sort.Direction dir = Sort.Direction.ASC;
                if (field.startsWith("-")) {
                    dir = Sort.Direction.DESC;
                    field = field.substring(1);
                }
                orders.add(new Sort.Order(dir, field));
            }
            sortObj = Sort.by(orders);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Specification<Employee> spec = buildSpec(filter);
        Page<Employee> p = repository.findAll(spec, pageable);
        return EmployeePage.builder()
                .content(p.getContent())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .build();
    }

    private Specification<Employee> buildSpec(EmployeeFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) return cb.conjunction();
            List<Predicate> predicates = new ArrayList<>();
            if (filter.nameContains() != null && !filter.nameContains().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.nameContains().toLowerCase() + "%"));
            }
            if (filter.minAge() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), filter.minAge()));
            }
            if (filter.maxAge() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), filter.maxAge()));
            }
            if (filter.department() != null && !filter.department().isBlank()) {
                predicates.add(cb.equal(root.get("department"), filter.department()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Employee getById(Long id) {
        Optional<Employee> o = repository.findById(id);
        return o.orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
    }

    @Transactional
    public Employee createEmployee(EmployeeInput input) {
        Employee e = Employee.builder()
                .name(input.name())
                .age(input.age())
                .department(input.department())
                .skills(input.skills())
                .attendance(input.attendance())
                .build();
        return repository.save(e);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeInput input) {
        Employee existing = repository.findById(id).orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
        existing.setName(input.name());
        existing.setAge(input.age());
        existing.setDepartment(input.department());
        existing.setSkills(input.skills());
        existing.setAttendance(input.attendance());
        return repository.save(existing);
    }

    @Transactional
    public boolean deleteEmployee(Long id) {
        // Check existence first
        if (!repository.existsById(id)) {
            throw new NotFoundException("Employee not found with id: " + id);
        }
        repository.deleteById(id);
        return true;
    }

    public List<Employee> findByIds(List<Long> ids) {
        return repository.findByIdIn(ids);
    }
}
