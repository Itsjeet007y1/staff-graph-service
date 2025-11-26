package com.staff.employee.dataloader;

import com.staff.employee.model.Employee;
import com.staff.employee.service.EmployeeService;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class EmployeeDataLoader {

    private final EmployeeService service;

    public EmployeeDataLoader(EmployeeService service) {
        this.service = service;
    }

    public DataLoader<Long, Employee> getDataLoader() {
        BatchLoader<Long, Employee> loader = ids -> CompletableFuture.supplyAsync(() -> {
            List<Long> idList = ids.stream().collect(Collectors.toList());
            List<Employee> employees = service.findByIds(idList);
            Map<Long, Employee> map = employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
            return idList.stream().map(map::get).collect(Collectors.toList());
        });
        return DataLoader.newDataLoader(loader);
    }
}
