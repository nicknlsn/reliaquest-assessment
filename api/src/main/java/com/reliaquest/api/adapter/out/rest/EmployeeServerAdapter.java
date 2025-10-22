package com.reliaquest.api.adapter.out.rest;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.out.DeleteEmployeePort;
import com.reliaquest.api.application.port.out.LoadEmployeeByIdPort;
import com.reliaquest.api.application.port.out.LoadEmployeesPort;
import com.reliaquest.api.application.port.out.SaveNewEmployeePort;
import com.reliaquest.api.common.OutAdapter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@OutAdapter
@RequiredArgsConstructor
public class EmployeeServerAdapter
        implements LoadEmployeesPort, LoadEmployeeByIdPort, SaveNewEmployeePort, DeleteEmployeePort {
    private final String employeeServerUrl = "http://localhost:8112/api/v1/employee";

    private final RestTemplate restTemplate;

    private final EmployeeMapper employeeMapper;

    @Override
    @Cacheable(cacheNames = "allEmployees")
    public List<Employee> loadAllEmployees() {
        List<Employee> employees = null;

        try {
            ResponseEntity<EmployeeServerResponse<List<EmployeeEntity>>> response = restTemplate.exchange(
                    employeeServerUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                employees = response.getBody().getData().stream()
                        .map(employeeMapper::toEmployee)
                        .toList();
            }
        } catch (Exception e) {
            log.error("An error occurred while trying to load employees from Employee Server", e);
            return null;
        }

        return employees;
    }

    @Override
    @Cacheable(cacheNames = "employeeById", key = "#id")
    public Employee loadEmployeeById(UUID id) {
        Employee employee = null;

        try {
            String url = String.format("%s/%s", employeeServerUrl, id.toString());
            ResponseEntity<EmployeeServerResponse<EmployeeEntity>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                employee = employeeMapper.toEmployee(response.getBody().getData());
            }
        } catch (Exception e) {
            if (((HttpClientErrorException) e).getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Employee with id {} not found", id);
            } else {
                log.error("An error occurred while trying to load employee by id from Employee Server", e);
            }
            return null;
        }

        return employee;
    }

    @Override
    @CacheEvict(cacheNames = "allEmployees", allEntries = true)
    public Employee saveNewEmployee(Employee employee) {
        Employee newEmployee = null;

        try {
            ResponseEntity<EmployeeServerResponse<EmployeeEntity>> response = restTemplate.exchange(
                    employeeServerUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(employee),
                    new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                newEmployee = employeeMapper.toEmployee(response.getBody().getData());
            }
        } catch (Exception e) {
            log.error("An error occurred while trying to save new employee to Employee Server", e);
            return null;
        }

        return newEmployee;
    }

    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "allEmployees", allEntries = true),
                @CacheEvict(cacheNames = "employeeById", key = "#uuid")
            })
    public String deleteEmployeeById(UUID uuid) {
        String deletedEmployee = null;

        // this will skip caching because we're invoking this method from within the same class; but this is desired
        // because it will ensure the employee exists before attempting to delete
        Employee employeeToDelete = loadEmployeeById(uuid);

        if (employeeToDelete == null) {
            log.info("Employee with id {} not found, cannot delete", uuid);
            return null;
        }

        try {
            // create a new employee object for the request body so that it only has the name, which is what the server
            // expects
            HttpEntity<Employee> requestEntity = new HttpEntity<>(
                    Employee.builder().name(employeeToDelete.getName()).build());
            ResponseEntity<EmployeeServerResponse<Boolean>> response = restTemplate.exchange(
                    employeeServerUrl, HttpMethod.DELETE, requestEntity, new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getData()) {
                deletedEmployee = employeeToDelete.getName();
            }
        } catch (Exception e) {
            log.error("An error occurred while trying to delete employee by id from Employee Server", e);
            return null;
        }

        return deletedEmployee;
    }
}
