package com.reliaquest.api.application.domain.service;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.in.GetAllEmployeesUseCase;
import com.reliaquest.api.application.port.in.GetEmployeeByIdUseCase;
import com.reliaquest.api.application.port.in.GetEmployeesByNameSearchUseCase;
import com.reliaquest.api.application.port.out.LoadEmployeeByIdPort;
import com.reliaquest.api.application.port.out.LoadEmployeesPort;
import com.reliaquest.api.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class EmployeesService implements GetAllEmployeesUseCase, GetEmployeesByNameSearchUseCase, GetEmployeeByIdUseCase {

    private final LoadEmployeesPort loadEmployeesPort;
    private final LoadEmployeeByIdPort loadEmployeeByIdPort;

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

    @Override
    public Employee getEmployeeById(UUID id) {
        return loadEmployeeByIdPort.loadEmployeeById(id);
    }
}
