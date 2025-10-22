package com.reliaquest.api.application.port.in;

import com.reliaquest.api.application.domain.model.Employee;

import java.util.UUID;

public interface GetEmployeeByIdUseCase {

    Employee getEmployeeById(UUID id);
}
