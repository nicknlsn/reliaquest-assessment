package com.reliaquest.api.adapter.out.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.reliaquest.api.application.domain.model.Employee;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for EmployeeMapper.
 * Tests the mapping between EmployeeEntity (external API representation) and Employee (domain model).
 */
class EmployeeMapperTest {

    private EmployeeMapper employeeMapper;

    @BeforeEach
    void setUp() {
        employeeMapper = new EmployeeMapper();
    }

    @Test
    void toEmployee_shouldMapAllFields_whenEntityIsValid() {
        // Given
        UUID id = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(id);
        entity.setEmployee_name("John Doe");
        entity.setEmployee_salary(75000);
        entity.setEmployee_age(30);
        entity.setEmployee_title("Software Engineer");
        entity.setEmployee_email("john.doe@example.com");

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getSalary()).isEqualTo(75000);
        assertThat(result.getAge()).isEqualTo(30);
        assertThat(result.getTitle()).isEqualTo("Software Engineer");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void toEmployee_shouldReturnNull_whenEntityIsNull() {
        // Given
        EmployeeEntity entity = null;

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEmployee_shouldHandleNullFields_inEntity() {
        // Given
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(null);
        entity.setEmployee_name(null);
        entity.setEmployee_salary(null);
        entity.setEmployee_age(null);
        entity.setEmployee_title(null);
        entity.setEmployee_email(null);

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isNull();
        assertThat(result.getSalary()).isNull();
        assertThat(result.getAge()).isNull();
        assertThat(result.getTitle()).isNull();
        assertThat(result.getEmail()).isNull();
    }

    @Test
    void toEmployee_shouldMapCorrectly_withMinimalData() {
        // Given - Only required fields
        UUID id = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(id);
        entity.setEmployee_name("Jane Smith");

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Jane Smith");
        assertThat(result.getSalary()).isNull();
        assertThat(result.getAge()).isNull();
        assertThat(result.getTitle()).isNull();
        assertThat(result.getEmail()).isNull();
    }

    @Test
    void toEmployee_shouldHandleZeroValues() {
        // Given
        UUID id = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(id);
        entity.setEmployee_name("Test Employee");
        entity.setEmployee_salary(0);
        entity.setEmployee_age(0);
        entity.setEmployee_title("");
        entity.setEmployee_email("");

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Test Employee");
        assertThat(result.getSalary()).isEqualTo(0);
        assertThat(result.getAge()).isEqualTo(0);
        assertThat(result.getTitle()).isEqualTo("");
        assertThat(result.getEmail()).isEqualTo("");
    }

    @Test
    void toEmployee_shouldHandleSpecialCharactersInName() {
        // Given
        UUID id = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(id);
        entity.setEmployee_name("Patrick O'Brien-Smith");
        entity.setEmployee_salary(80000);
        entity.setEmployee_age(35);
        entity.setEmployee_title("Senior Engineer");
        entity.setEmployee_email("patrick.obrien@example.com");

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Patrick O'Brien-Smith");
    }

    @Test
    void toEmployee_shouldHandleLargeSalaryValues() {
        // Given
        UUID id = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(id);
        entity.setEmployee_name("CEO");
        entity.setEmployee_salary(Integer.MAX_VALUE);
        entity.setEmployee_age(50);
        entity.setEmployee_title("Chief Executive Officer");
        entity.setEmployee_email("ceo@example.com");

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSalary()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void toEmployee_shouldMapFieldNamesCorrectly_fromSnakeCaseToCamelCase() {
        // Given - This test ensures the field name mapping is correct
        UUID id = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(id);
        entity.setEmployee_name("Field Test");
        entity.setEmployee_salary(50000);
        entity.setEmployee_age(25);
        entity.setEmployee_title("Tester");
        entity.setEmployee_email("test@example.com");

        // When
        Employee result = employeeMapper.toEmployee(entity);

        // Then - Verify snake_case entity fields map to camelCase domain fields
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(entity.getEmployee_name());
        assertThat(result.getSalary()).isEqualTo(entity.getEmployee_salary());
        assertThat(result.getAge()).isEqualTo(entity.getEmployee_age());
        assertThat(result.getTitle()).isEqualTo(entity.getEmployee_title());
        assertThat(result.getEmail()).isEqualTo(entity.getEmployee_email());
    }
}
