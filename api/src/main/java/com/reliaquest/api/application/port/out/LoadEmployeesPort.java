package com.reliaquest.api.application.port.out;

import com.reliaquest.api.application.domain.model.Employee;

import java.util.List;

public interface LoadEmployeesPort {

    List<Employee> loadAllEmployees();
}
