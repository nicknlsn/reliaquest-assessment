package com.reliaquest.api.application.domain.service;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.in.GetAllEmployeesUseCase;
import com.reliaquest.api.application.port.out.LoadEmployeesPort;
import com.reliaquest.api.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class EmployeeService implements GetAllEmployeesUseCase {

    private final LoadEmployeesPort loadEmployeesPort;

    @Override
    public List<Employee> getAllEmployees() {
        return loadEmployeesPort.loadAllEmployees();
    }
}
