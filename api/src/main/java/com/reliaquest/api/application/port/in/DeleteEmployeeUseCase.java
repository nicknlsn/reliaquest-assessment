package com.reliaquest.api.application.port.in;

import java.util.UUID;

public interface DeleteEmployeeUseCase {

    String deleteEmployeeById(UUID uuid);
}
