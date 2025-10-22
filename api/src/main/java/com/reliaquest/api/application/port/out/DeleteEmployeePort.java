package com.reliaquest.api.application.port.out;

import java.util.UUID;

public interface DeleteEmployeePort {

    String deleteEmployeeById(UUID uuid);
}
