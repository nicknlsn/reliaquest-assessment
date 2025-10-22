package com.reliaquest.api.adapter.in.rest;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.in.CreateEmployeeUseCase;
import com.reliaquest.api.application.port.in.DeleteEmployeeUseCase;
import com.reliaquest.api.application.port.in.GetAllEmployeesUseCase;
import com.reliaquest.api.application.port.in.GetEmployeeByIdUseCase;
import com.reliaquest.api.application.port.in.GetEmployeesByNameSearchUseCase;
import com.reliaquest.api.application.port.in.GetHighestSalaryUseCase;
import com.reliaquest.api.application.port.in.GetTopTenEarnerNamesUseCase;
import com.reliaquest.api.controller.IEmployeeController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/employee")
public class EmployeeController implements IEmployeeController<Employee, Employee> {

    private final GetAllEmployeesUseCase getAllEmployeesUseCase;
    private final GetEmployeesByNameSearchUseCase getEmployeesByNameSearchUseCase;
    private final GetEmployeeByIdUseCase getEmployeeByIdUseCase;
    private final GetHighestSalaryUseCase getHighestSalaryUseCase;
    private final GetTopTenEarnerNamesUseCase getTopTenEarnerNamesUseCase;
    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final DeleteEmployeeUseCase deleteEmployeeUseCase;

    @Override
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Request to get all employees");
        return new ResponseEntity<>(getAllEmployeesUseCase.getAllEmployees(), HttpStatus.OK);
    }

    @Override
    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        log.info("Request to get employees by name search with search string: {}", searchString);
        return new ResponseEntity<>(
                getEmployeesByNameSearchUseCase.getEmployeesByNameSearch(searchString), HttpStatus.OK);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        log.info("Request to get employee by id {}", id);
        UUID uuid = validateAndParseUUID(id);
        Employee employee = getEmployeeByIdUseCase.getEmployeeById(uuid);
        return employee == null
                ? new ResponseEntity<>(null, HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(employee, HttpStatus.OK);
    }

    /**
     * Validates and parses a UUID string.
     * This validation is performed at the controller level as recommended by Spring Boot best practices.
     *
     * @param id the string to validate and parse as UUID
     * @return the parsed UUID
     * @throws IllegalArgumentException if the string is not a valid UUID format
     */
    private UUID validateAndParseUUID(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("UUID cannot be null or empty");
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }
    }

    @Override
    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Request to get highest salary of employees");
        return new ResponseEntity<>(getHighestSalaryUseCase.getHighestSalary(), HttpStatus.OK);
    }

    @Override
    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Request to get top ten employee names");
        return new ResponseEntity<>(getTopTenEarnerNamesUseCase.getTopTenEarnerNames(), HttpStatus.OK);
    }

    @Override
    @PostMapping
    public ResponseEntity<Employee> createEmployee(Employee employeeInput) {
        log.info("Request to create a new employee: {}", employeeInput);
        return new ResponseEntity<>(createEmployeeUseCase.createEmployee(employeeInput), HttpStatus.CREATED);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("Request to delete employee by id {}", id);
        UUID uuid = validateAndParseUUID(id);
        String name = deleteEmployeeUseCase.deleteEmployeeById(uuid);
        return name == null ? new ResponseEntity<>(null, HttpStatus.NOT_FOUND) : new ResponseEntity<>(name, HttpStatus.OK);
    }
}
