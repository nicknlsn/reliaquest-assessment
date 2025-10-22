package com.reliaquest.api.adapter.in.rest;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.in.GetAllEmployeesUseCase;
import com.reliaquest.api.application.port.in.GetEmployeesByNameSearchUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer test for EmployeeController using @WebMvcTest.
 * This test focuses on the web layer only, mocking all dependencies.
 *
 * Following patterns from: https://reflectoring.io/spring-boot-web-controller-test/
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAllEmployeesUseCase getAllEmployeesUseCase;

    @MockBean
    private GetEmployeesByNameSearchUseCase getEmployeesByNameSearchUseCase;

    @Test
    void getAllEmployees_shouldReturnListOfEmployees_whenEmployeesExist() throws Exception {
        // Given
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();

        Employee employee1 = Employee.builder()
                .id(employeeId1)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .id(employeeId2)
                .name("Jane Smith")
                .salary(85000)
                .age(28)
                .title("Senior Software Engineer")
                .email("jane.smith@example.com")
                .build();

        List<Employee> employees = Arrays.asList(employee1, employee2);
        when(getAllEmployeesUseCase.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(employeeId1.toString())))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[0].salary", is(75000)))
                .andExpect(jsonPath("$[0].age", is(30)))
                .andExpect(jsonPath("$[0].title", is("Software Engineer")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[1].id", is(employeeId2.toString())))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")))
                .andExpect(jsonPath("$[1].salary", is(85000)))
                .andExpect(jsonPath("$[1].age", is(28)))
                .andExpect(jsonPath("$[1].title", is("Senior Software Engineer")))
                .andExpect(jsonPath("$[1].email", is("jane.smith@example.com")));
    }

    @Test
    void getAllEmployees_shouldReturnEmptyList_whenNoEmployeesExist() throws Exception {
        // Given
        when(getAllEmployeesUseCase.getAllEmployees()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllEmployees_shouldReturnSingleEmployee_whenOnlyOneEmployeeExists() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("Alice Johnson")
                .salary(95000)
                .age(35)
                .title("Lead Developer")
                .email("alice.johnson@example.com")
                .build();

        List<Employee> employees = Collections.singletonList(employee);
        when(getAllEmployeesUseCase.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(employeeId.toString())))
                .andExpect(jsonPath("$[0].name", is("Alice Johnson")))
                .andExpect(jsonPath("$[0].salary", is(95000)))
                .andExpect(jsonPath("$[0].age", is(35)))
                .andExpect(jsonPath("$[0].title", is("Lead Developer")))
                .andExpect(jsonPath("$[0].email", is("alice.johnson@example.com")));
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnMatchingEmployees_whenSearchStringMatches() throws Exception {
        // Given
        String searchString = "John";
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();

        Employee employee1 = Employee.builder()
                .id(employeeId1)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .id(employeeId2)
                .name("Johnny Smith")
                .salary(80000)
                .age(32)
                .title("Developer")
                .email("johnny.smith@example.com")
                .build();

        List<Employee> employees = Arrays.asList(employee1, employee2);
        when(getEmployeesByNameSearchUseCase.getEmployeeByNameSearch(searchString)).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/{searchString}", searchString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(employeeId1.toString())))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[0].salary", is(75000)))
                .andExpect(jsonPath("$[0].age", is(30)))
                .andExpect(jsonPath("$[0].title", is("Software Engineer")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[1].id", is(employeeId2.toString())))
                .andExpect(jsonPath("$[1].name", is("Johnny Smith")))
                .andExpect(jsonPath("$[1].salary", is(80000)))
                .andExpect(jsonPath("$[1].age", is(32)))
                .andExpect(jsonPath("$[1].title", is("Developer")))
                .andExpect(jsonPath("$[1].email", is("johnny.smith@example.com")));
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnEmptyList_whenNoEmployeesMatch() throws Exception {
        // Given
        String searchString = "NonExistent";
        when(getEmployeesByNameSearchUseCase.getEmployeeByNameSearch(searchString)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/{searchString}", searchString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnSingleEmployee_whenOneEmployeeMatches() throws Exception {
        // Given
        String searchString = "Alice";
        UUID employeeId = UUID.randomUUID();

        Employee employee = Employee.builder()
                .id(employeeId)
                .name("Alice Johnson")
                .salary(95000)
                .age(35)
                .title("Lead Developer")
                .email("alice.johnson@example.com")
                .build();

        List<Employee> employees = Collections.singletonList(employee);
        when(getEmployeesByNameSearchUseCase.getEmployeeByNameSearch(searchString)).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/{searchString}", searchString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(employeeId.toString())))
                .andExpect(jsonPath("$[0].name", is("Alice Johnson")))
                .andExpect(jsonPath("$[0].salary", is(95000)))
                .andExpect(jsonPath("$[0].age", is(35)))
                .andExpect(jsonPath("$[0].title", is("Lead Developer")))
                .andExpect(jsonPath("$[0].email", is("alice.johnson@example.com")));
    }

    @Test
    void getEmployeesByNameSearch_shouldHandleSpecialCharacters_inSearchString() throws Exception {
        // Given
        String searchString = "O'Brien";
        UUID employeeId = UUID.randomUUID();

        Employee employee = Employee.builder()
                .id(employeeId)
                .name("Patrick O'Brien")
                .salary(88000)
                .age(40)
                .title("Senior Developer")
                .email("patrick.obrien@example.com")
                .build();

        List<Employee> employees = Collections.singletonList(employee);
        when(getEmployeesByNameSearchUseCase.getEmployeeByNameSearch(searchString)).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/{searchString}", searchString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(employeeId.toString())))
                .andExpect(jsonPath("$[0].name", is("Patrick O'Brien")))
                .andExpect(jsonPath("$[0].salary", is(88000)))
                .andExpect(jsonPath("$[0].age", is(40)))
                .andExpect(jsonPath("$[0].title", is("Senior Developer")))
                .andExpect(jsonPath("$[0].email", is("patrick.obrien@example.com")));
    }
}
