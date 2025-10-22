package com.reliaquest.api.adapter.in.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.in.GetAllEmployeesUseCase;
import com.reliaquest.api.application.port.in.GetEmployeeByIdUseCase;
import com.reliaquest.api.application.port.in.GetEmployeesByNameSearchUseCase;
import com.reliaquest.api.application.port.in.GetHighestSalaryUseCase;
import com.reliaquest.api.application.port.in.GetTopTenEarnerNamesUseCase;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @MockBean
    private GetEmployeeByIdUseCase getEmployeeByIdUseCase;

    @MockBean
    private GetHighestSalaryUseCase getHighestSalaryUseCase;

    @MockBean
    private GetTopTenEarnerNamesUseCase getTopTenEarnerNamesUseCase;

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
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
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
        when(getEmployeesByNameSearchUseCase.getEmployeesByNameSearch(searchString))
                .thenReturn(employees);

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
        when(getEmployeesByNameSearchUseCase.getEmployeesByNameSearch(searchString))
                .thenReturn(Collections.emptyList());

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
        when(getEmployeesByNameSearchUseCase.getEmployeesByNameSearch(searchString))
                .thenReturn(employees);

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
        when(getEmployeesByNameSearchUseCase.getEmployeesByNameSearch(searchString))
                .thenReturn(employees);

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

    @Test
    void getEmployeeById_shouldReturnEmployee_whenValidUUIDProvided() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@example.com")
                .build();

        when(getEmployeeByIdUseCase.getEmployeeById(employeeId)).thenReturn(employee);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", employeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employeeId.toString())))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.salary", is(75000)))
                .andExpect(jsonPath("$.age", is(30)))
                .andExpect(jsonPath("$.title", is("Software Engineer")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    void getEmployeeById_shouldReturnNotFound_whenEmployeeDoesNotExist() throws Exception {
        // Given - Valid UUID but employee doesn't exist
        UUID nonExistentId = UUID.randomUUID();
        when(getEmployeeByIdUseCase.getEmployeeById(nonExistentId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", nonExistentId.toString())).andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeById_shouldReturnBadRequest_whenInvalidUUIDProvided() throws Exception {
        // Given - Invalid UUID format
        String invalidId = "not-a-uuid";

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void getEmployeeById_shouldReturnBadRequest_whenUUIDHasInvalidFormat() throws Exception {
        // Given - Partially valid UUID format but incorrect
        String invalidId = "123e4567-e89b-12d3-a456";

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void getEmployeeById_shouldReturnBadRequest_whenUUIDIsEmpty() throws Exception {
        // Given - Empty string
        String emptyId = "";

        // When & Then - Note: Spring will match this to getAllEmployees instead due to routing
        // So we test with whitespace instead
        String whitespaceId = "   ";
        mockMvc.perform(get("/api/v1/employee/{id}", whitespaceId)).andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeeById_shouldReturnBadRequest_whenUUIDContainsSpecialCharacters() throws Exception {
        // Given - UUID with invalid characters
        String invalidId = "123e4567-e89b-12d3-a456-426614174000!";

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void getEmployeeById_shouldAcceptUppercaseUUID() throws Exception {
        // Given - Valid UUID in uppercase
        UUID employeeId = UUID.randomUUID();
        String uppercaseId = employeeId.toString().toUpperCase();

        Employee employee = Employee.builder()
                .id(employeeId)
                .name("Jane Smith")
                .salary(85000)
                .age(28)
                .title("Senior Engineer")
                .email("jane.smith@example.com")
                .build();

        when(getEmployeeByIdUseCase.getEmployeeById(employeeId)).thenReturn(employee);

        // When & Then - UUID validation should accept uppercase format
        mockMvc.perform(get("/api/v1/employee/{id}", uppercaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Smith")));
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnHighestSalary_whenEmployeesExist() throws Exception {
        // Given
        Integer highestSalary = 95000;
        when(getHighestSalaryUseCase.getHighestSalary()).thenReturn(highestSalary);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(95000)));
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnNull_whenNoEmployeesExist() throws Exception {
        // Given
        when(getHighestSalaryUseCase.getHighestSalary()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnZero_whenHighestSalaryIsZero() throws Exception {
        // Given
        Integer highestSalary = 0;
        when(getHighestSalaryUseCase.getHighestSalary()).thenReturn(highestSalary);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(0)));
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnHighestSalary_whenMultipleEmployeesWithDifferentSalaries()
            throws Exception {
        // Given
        Integer highestSalary = 150000;
        when(getHighestSalaryUseCase.getHighestSalary()).thenReturn(highestSalary);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(150000)));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnTopTenNames_whenMoreThanTenEmployeesExist() throws Exception {
        // Given
        List<String> topTenNames = Arrays.asList(
                "Employee 1",
                "Employee 2",
                "Employee 3",
                "Employee 4",
                "Employee 5",
                "Employee 6",
                "Employee 7",
                "Employee 8",
                "Employee 9",
                "Employee 10");
        when(getTopTenEarnerNamesUseCase.getTopTenEarnerNames()).thenReturn(topTenNames);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0]", is("Employee 1")))
                .andExpect(jsonPath("$[9]", is("Employee 10")));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnAllNames_whenFewerThanTenEmployeesExist() throws Exception {
        // Given
        List<String> names = Arrays.asList("Alice Johnson", "Bob Smith", "Charlie Brown");
        when(getTopTenEarnerNamesUseCase.getTopTenEarnerNames()).thenReturn(names);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Alice Johnson")))
                .andExpect(jsonPath("$[1]", is("Bob Smith")))
                .andExpect(jsonPath("$[2]", is("Charlie Brown")));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnEmptyList_whenNoEmployeesExist() throws Exception {
        // Given
        when(getTopTenEarnerNamesUseCase.getTopTenEarnerNames()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnExactlyTenNames_whenExactlyTenEmployeesExist()
            throws Exception {
        // Given
        List<String> names = Arrays.asList(
                "Name1", "Name2", "Name3", "Name4", "Name5", "Name6", "Name7", "Name8", "Name9", "Name10");
        when(getTopTenEarnerNamesUseCase.getTopTenEarnerNames()).thenReturn(names);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }
}
