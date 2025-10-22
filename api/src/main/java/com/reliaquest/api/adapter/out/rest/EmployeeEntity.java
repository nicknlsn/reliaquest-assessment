package com.reliaquest.api.adapter.out.rest;

import java.util.UUID;
import lombok.Data;

@Data
class EmployeeEntity {

    private UUID id;
    private String employee_name;
    private Integer employee_salary;
    private Integer employee_age;
    private String employee_title;
    private String employee_email;
}
