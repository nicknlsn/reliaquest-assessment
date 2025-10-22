package com.reliaquest.api.application.domain.service;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.in.GetAllEmployeesUseCase;
import com.reliaquest.api.application.port.in.GetEmployeeByIdUseCase;
import com.reliaquest.api.application.port.in.GetEmployeesByNameSearchUseCase;
import com.reliaquest.api.application.port.in.GetHighestSalaryUseCase;
import com.reliaquest.api.application.port.in.GetTopTenEarnerNamesUseCase;
import com.reliaquest.api.application.port.out.LoadEmployeeByIdPort;
import com.reliaquest.api.application.port.out.LoadEmployeesPort;
import com.reliaquest.api.common.UseCase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class EmployeesService
        implements GetAllEmployeesUseCase,
                GetEmployeesByNameSearchUseCase,
                GetEmployeeByIdUseCase,
                GetHighestSalaryUseCase,
                GetTopTenEarnerNamesUseCase {

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

    @Override
    public Integer getHighestSalary() {
        Integer highestSalary = null;

        List<Employee> allEmployees = loadEmployeesPort.loadAllEmployees();
        for (Employee employee : allEmployees) {
            if (highestSalary == null || employee.getSalary() > highestSalary) {
                highestSalary = employee.getSalary();
            }
        }

        return highestSalary;
    }

    @Override
    public List<String> getTopTenEarnerNames() {
        List<String> topTenEarnerNames = new ArrayList<>();

        List<Employee> allEmployees = loadEmployeesPort.loadAllEmployees();
        allEmployees.stream()
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .limit(10)
                .forEach(employee -> topTenEarnerNames.add(employee.getName()));

        return topTenEarnerNames;
    }
}
