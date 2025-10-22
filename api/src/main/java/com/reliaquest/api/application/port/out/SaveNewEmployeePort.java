package com.reliaquest.api.application.port.out;

import com.reliaquest.api.application.domain.model.Employee;

public interface SaveNewEmployeePort {

    Employee saveNewEmployee(Employee employee);
}
