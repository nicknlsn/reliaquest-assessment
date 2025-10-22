package com.reliaquest.api.application.port.in;

import com.reliaquest.api.application.domain.model.Employee;

import java.util.List;

public interface GetEmployeesByNameSearchUseCase {

    List<Employee> getEmployeeByNameSearch(String name);
}
