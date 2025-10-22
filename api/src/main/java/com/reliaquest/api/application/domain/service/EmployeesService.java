package com.reliaquest.api.application.domain.service;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.in.GetAllEmployeesUseCase;
import com.reliaquest.api.application.port.in.GetEmployeesByNameSearchUseCase;
import com.reliaquest.api.application.port.out.LoadEmployeesPort;
import com.reliaquest.api.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class EmployeesService implements GetAllEmployeesUseCase, GetEmployeesByNameSearchUseCase {

    private final LoadEmployeesPort loadEmployeesPort;

    @Override
    public List<Employee> getAllEmployees() {
        return loadEmployeesPort.loadAllEmployees();
    }

    @Override
    public List<Employee> getEmployeeByNameSearch(String name) {
        List<Employee> allEmployees = loadEmployeesPort.loadAllEmployees();

        return allEmployees.stream()
                .filter(employee -> employee.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }
}
