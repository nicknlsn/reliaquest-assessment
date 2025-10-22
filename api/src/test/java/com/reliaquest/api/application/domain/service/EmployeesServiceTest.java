package com.reliaquest.api.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.application.domain.model.Employee;
import com.reliaquest.api.application.port.out.LoadEmployeesPort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for EmployeesService.
 * These are plain unit tests without Spring context, using Mockito for mocking dependencies.
 *
 * Following patterns from: https://reflectoring.io/unit-testing-spring-boot/
 */
@ExtendWith(MockitoExtension.class)
class EmployeesServiceTest {

    @Mock
    private LoadEmployeesPort loadEmployeesPort;

    @InjectMocks
    private EmployeesService employeesService;

    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        // Arrange - Create test data used across multiple tests
        Employee employee1 = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Jane Smith")
                .salary(85000)
                .age(28)
                .title("Senior Software Engineer")
                .email("jane.smith@example.com")
                .build();

        Employee employee3 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Johnny Appleseed")
                .salary(65000)
                .age(25)
                .title("Junior Developer")
                .email("johnny.appleseed@example.com")
                .build();

        Employee employee4 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Alice Johnson")
                .salary(95000)
                .age(35)
                .title("Lead Developer")
                .email("alice.johnson@example.com")
                .build();

        testEmployees = Arrays.asList(employee1, employee2, employee3, employee4);
    }

    @Test
    void getAllEmployees_shouldReturnAllEmployees_whenPortReturnsEmployees() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getAllEmployees();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result).isEqualTo(testEmployees);
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getAllEmployees_shouldReturnEmptyList_whenPortReturnsEmptyList() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(Collections.emptyList());

        // Act
        List<Employee> result = employeesService.getAllEmployees();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnMatchingEmployees_whenSearchStringMatchesMultiple() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("John");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("John Doe", "Johnny Appleseed", "Alice Johnson");
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_shouldBeCaseInsensitive() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act - Search with different cases
        List<Employee> resultLowerCase = employeesService.getEmployeesByNameSearch("john");
        List<Employee> resultUpperCase = employeesService.getEmployeesByNameSearch("JOHN");
        List<Employee> resultMixedCase = employeesService.getEmployeesByNameSearch("JoHn");

        // Assert - All should return the same results
        assertThat(resultLowerCase).hasSize(3);
        assertThat(resultUpperCase).hasSize(3);
        assertThat(resultMixedCase).hasSize(3);
        assertThat(resultLowerCase).isEqualTo(resultUpperCase);
        assertThat(resultUpperCase).isEqualTo(resultMixedCase);
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnEmptyList_whenNoEmployeesMatch() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("NonExistentName");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getEmployeeByNameSearch_shouldReturnSingleEmployees_whenOnlyOneMatches() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("Jane");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane Smith");
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_shouldMatchPartialName() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("son");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnAllEmployees_whenSearchStringIsEmpty() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result).isEqualTo(testEmployees);
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_shouldMatchFirstName() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("Alice");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
    }

    @Test
    void getEmployeesByNameSearch_shouldMatchLastName() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("Smith");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane Smith");
    }

    @Test
    void getEmployeesByNameSearch_shouldHandleSpecialCharacters() {
        // Arrange
        Employee specialEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("Patrick O'Brien")
                .salary(80000)
                .age(40)
                .title("Developer")
                .email("patrick.obrien@example.com")
                .build();

        List<Employee> employeesWithSpecialChars = Collections.singletonList(specialEmployee);
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(employeesWithSpecialChars);

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("O'Brien");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Patrick O'Brien");
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnEmptyList_whenPortReturnsEmptyList() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(Collections.emptyList());

        // Act
        List<Employee> result = employeesService.getEmployeesByNameSearch("John");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getHighestSalary_shouldReturnHighestSalary_whenEmployeesExist() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(testEmployees);

        // Act
        Integer result = employeesService.getHighestSalary();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(95000); // Alice Johnson has the highest salary
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getHighestSalary_shouldReturnNull_whenNoEmployeesExist() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(Collections.emptyList());

        // Act
        Integer result = employeesService.getHighestSalary();

        // Assert
        assertThat(result).isNull();
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getHighestSalary_shouldReturnSalary_whenOnlyOneEmployeeExists() {
        // Arrange
        Employee singleEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("Single Employee")
                .salary(50000)
                .age(25)
                .title("Junior Developer")
                .email("single@example.com")
                .build();

        when(loadEmployeesPort.loadAllEmployees()).thenReturn(Collections.singletonList(singleEmployee));

        // Act
        Integer result = employeesService.getHighestSalary();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(50000);
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getHighestSalary_shouldReturnHighestSalary_whenMultipleEmployeesHaveSameSalary() {
        // Arrange
        Employee employee1 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee One")
                .salary(80000)
                .age(30)
                .title("Developer")
                .email("employee1@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee Two")
                .salary(80000)
                .age(28)
                .title("Developer")
                .email("employee2@example.com")
                .build();

        List<Employee> employeesWithSameSalary = Arrays.asList(employee1, employee2);
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(employeesWithSameSalary);

        // Act
        Integer result = employeesService.getHighestSalary();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(80000);
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getHighestSalary_shouldHandleZeroSalary() {
        // Arrange
        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("Volunteer")
                .salary(0)
                .age(22)
                .title("Intern")
                .email("volunteer@example.com")
                .build();

        when(loadEmployeesPort.loadAllEmployees()).thenReturn(Collections.singletonList(employee));

        // Act
        Integer result = employeesService.getHighestSalary();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0);
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getTopTenEarnerNames_shouldReturnTopTenNames_whenMoreThanTenEmployeesExist() {
        // Arrange - Create 15 employees with varying salaries
        List<Employee> employees = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            employees.add(Employee.builder()
                    .id(UUID.randomUUID())
                    .name("Employee " + i)
                    .salary(100000 + (i * 1000)) // Salaries: 101000, 102000, ..., 115000
                    .age(30)
                    .title("Developer")
                    .email("employee" + i + "@example.com")
                    .build());
        }

        when(loadEmployeesPort.loadAllEmployees()).thenReturn(employees);

        // Act
        List<String> result = employeesService.getTopTenEarnerNames();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(10);
        // Highest earners should be Employee 15 down to Employee 6 (descending order by salary)
        assertThat(result)
                .containsExactly(
                        "Employee 15",
                        "Employee 14",
                        "Employee 13",
                        "Employee 12",
                        "Employee 11",
                        "Employee 10",
                        "Employee 9",
                        "Employee 8",
                        "Employee 7",
                        "Employee 6");
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getTopTenEarnerNames_shouldReturnAllNames_whenFewerThanTenEmployeesExist() {
        // Arrange - Create 5 employees
        List<Employee> employees = Arrays.asList(
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("High Earner")
                        .salary(100000)
                        .age(35)
                        .title("Senior Dev")
                        .email("high@example.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Mid Earner")
                        .salary(80000)
                        .age(30)
                        .title("Developer")
                        .email("mid@example.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Low Earner")
                        .salary(60000)
                        .age(25)
                        .title("Junior Dev")
                        .email("low@example.com")
                        .build());

        when(loadEmployeesPort.loadAllEmployees()).thenReturn(employees);

        // Act
        List<String> result = employeesService.getTopTenEarnerNames();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("High Earner", "Mid Earner", "Low Earner");
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getTopTenEarnerNames_shouldReturnEmptyList_whenNoEmployeesExist() {
        // Arrange
        when(loadEmployeesPort.loadAllEmployees()).thenReturn(Collections.emptyList());

        // Act
        List<String> result = employeesService.getTopTenEarnerNames();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getTopTenEarnerNames_shouldReturnNamesInDescendingOrderBySalary() {
        // Arrange - Create employees with specific salaries (not in order)
        List<Employee> employees = Arrays.asList(
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Middle")
                        .salary(75000)
                        .age(30)
                        .title("Developer")
                        .email("middle@example.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Highest")
                        .salary(100000)
                        .age(35)
                        .title("Senior")
                        .email("highest@example.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Lowest")
                        .salary(50000)
                        .age(25)
                        .title("Junior")
                        .email("lowest@example.com")
                        .build());

        when(loadEmployeesPort.loadAllEmployees()).thenReturn(employees);

        // Act
        List<String> result = employeesService.getTopTenEarnerNames();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Highest", "Middle", "Lowest");
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getTopTenEarnerNames_shouldHandleEmployeesWithSameSalary() {
        // Arrange - Create employees where some have the same salary
        List<Employee> employees = Arrays.asList(
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Employee A")
                        .salary(80000)
                        .age(30)
                        .title("Developer")
                        .email("a@example.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Employee B")
                        .salary(80000)
                        .age(28)
                        .title("Developer")
                        .email("b@example.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Employee C")
                        .salary(90000)
                        .age(35)
                        .title("Senior Dev")
                        .email("c@example.com")
                        .build());

        when(loadEmployeesPort.loadAllEmployees()).thenReturn(employees);

        // Act
        List<String> result = employeesService.getTopTenEarnerNames();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("Employee C"); // Highest salary first
        // A and B both have 80000, order between them may vary but both should be present
        assertThat(result).contains("Employee A", "Employee B");
        verify(loadEmployeesPort).loadAllEmployees();
    }

    @Test
    void getTopTenEarnerNames_shouldReturnExactlyTenNames_whenExactlyTenEmployeesExist() {
        // Arrange - Create exactly 10 employees
        List<Employee> employees = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            employees.add(Employee.builder()
                    .id(UUID.randomUUID())
                    .name("Employee " + i)
                    .salary(50000 + (i * 1000))
                    .age(30)
                    .title("Developer")
                    .email("employee" + i + "@example.com")
                    .build());
        }

        when(loadEmployeesPort.loadAllEmployees()).thenReturn(employees);

        // Act
        List<String> result = employeesService.getTopTenEarnerNames();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(10);
        verify(loadEmployeesPort).loadAllEmployees();
    }
}
