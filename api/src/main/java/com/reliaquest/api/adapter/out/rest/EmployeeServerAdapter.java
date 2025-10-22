package com.reliaquest.api.adapter.out.rest;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.out.LoadEmployeesPort;
import com.reliaquest.api.common.OutAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@OutAdapter
@RequiredArgsConstructor
public class EmployeeServerAdapter implements LoadEmployeesPort {

    private final String employeeServerUrl = "http://localhost:8112/api/v1/employee";

    private final RestTemplate restTemplate;
    private final EmployeeMapper employeeMapper;

    @Override
    public List<Employee> loadAllEmployees() {
        List<Employee> employees = null;

        try {
            ResponseEntity<EmployeeServerResponse<List<EmployeeEntity>>> response = restTemplate.exchange(employeeServerUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                employees = response.getBody().getData().stream().map(employeeMapper::toEmployee).toList();
            }
        } catch (Exception e) {
            log.error("An error occurred while trying to load employees from Employee Server", e);
            return null;
        }

        return employees;
    }
}
