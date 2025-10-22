package com.reliaquest.api.application.port.in;

import com.reliaquest.api.application.domain.model.Employee;

public interface CreateEmployeeUseCase {

    Employee createEmployee(Employee employee);
}
