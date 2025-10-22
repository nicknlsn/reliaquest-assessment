package com.reliaquest.api.application.port.out;

import com.reliaquest.api.application.domain.model.Employee;

import java.util.UUID;

public interface DeleteEmployeePort {

    String deleteEmployeeById(UUID uuid);
}
