package com.reliaquest.api.application.port.out;

import com.reliaquest.api.application.domain.model.Employee;

import java.util.UUID;

public interface LoadEmployeeByIdPort {

    Employee loadEmployeeById(UUID id);
}
