package com.reliaquest.api.adapter.out.rest;

import lombok.Data;

import java.util.UUID;

@Data
class EmployeeEntity {

    private UUID id;
    private String employee_name;
    private Integer employee_salary;
    private Integer employee_age;
    private String employee_title;
    private String employee_email;
}
