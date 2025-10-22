package com.reliaquest.api.adapter.out.rest;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.common.Mapper;

@Mapper
class EmployeeMapper {

    Employee toEmployee(EmployeeEntity employeeEntity) {
        if (employeeEntity == null) {
            return null;
        }

        return Employee.builder()
                .id(employeeEntity.getId())
                .name(employeeEntity.getEmployee_name())
                .salary(employeeEntity.getEmployee_salary())
                .age(employeeEntity.getEmployee_age())
                .title(employeeEntity.getEmployee_title())
                .email(employeeEntity.getEmployee_email())
                .build();
    }
}
