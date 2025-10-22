package com.reliaquest.api.adapter.out.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.application.domain.model.Employee;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for EmployeeServerAdapter.
 * Tests the adapter layer that communicates with the external employee server.
 *
 * Note: These are pure unit tests using Mockito. They do NOT test caching behavior
 * because @Mock bypasses Spring's proxy-based caching. For cache behavior tests,
 * see EmployeeServerAdapterCacheTest.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServerAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServerAdapter employeeServerAdapter;

    private EmployeeEntity testEntity1;
    private EmployeeEntity testEntity2;
    private Employee testEmployee1;
    private Employee testEmployee2;

    @BeforeEach
    void setUp() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        testEntity1 = new EmployeeEntity();
        testEntity1.setId(id1);
        testEntity1.setEmployee_name("John Doe");
        testEntity1.setEmployee_salary(75000);
        testEntity1.setEmployee_age(30);
        testEntity1.setEmployee_title("Software Engineer");
        testEntity1.setEmployee_email("john.doe@example.com");

        testEntity2 = new EmployeeEntity();
        testEntity2.setId(id2);
        testEntity2.setEmployee_name("Jane Smith");
        testEntity2.setEmployee_salary(85000);
        testEntity2.setEmployee_age(28);
        testEntity2.setEmployee_title("Senior Software Engineer");
        testEntity2.setEmployee_email("jane.smith@example.com");

        testEmployee1 = Employee.builder()
                .id(id1)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@example.com")
                .build();

        testEmployee2 = Employee.builder()
                .id(id2)
                .name("Jane Smith")
                .salary(85000)
                .age(28)
                .title("Senior Software Engineer")
                .email("jane.smith@example.com")
                .build();
    }

    // loadAllEmployees tests

    @Test
    void loadAllEmployees_shouldReturnEmployeeList_whenServerReturnsSuccessfulResponse() {
        // Given
        List<EmployeeEntity> entities = Arrays.asList(testEntity1, testEntity2);
        EmployeeServerResponse<List<EmployeeEntity>> serverResponse = new EmployeeServerResponse<>();
        serverResponse.setData(entities);
        serverResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<List<EmployeeEntity>>> responseEntity =
                new ResponseEntity<>(serverResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        when(employeeMapper.toEmployee(testEntity1)).thenReturn(testEmployee1);
        when(employeeMapper.toEmployee(testEntity2)).thenReturn(testEmployee2);

        // When
        List<Employee> result = employeeServerAdapter.loadAllEmployees();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testEmployee1, testEmployee2);
        verify(employeeMapper).toEmployee(testEntity1);
        verify(employeeMapper).toEmployee(testEntity2);
    }

    @Test
    void loadAllEmployees_shouldReturnEmptyList_whenServerReturnsEmptyList() {
        // Given
        EmployeeServerResponse<List<EmployeeEntity>> serverResponse = new EmployeeServerResponse<>();
        serverResponse.setData(Collections.emptyList());
        serverResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<List<EmployeeEntity>>> responseEntity =
                new ResponseEntity<>(serverResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        List<Employee> result = employeeServerAdapter.loadAllEmployees();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void loadAllEmployees_shouldReturnNull_whenResponseBodyIsNull() {
        // Given
        ResponseEntity<EmployeeServerResponse<List<EmployeeEntity>>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        List<Employee> result = employeeServerAdapter.loadAllEmployees();

        // Then
        assertThat(result).isNull();
    }

    @Test
    void loadAllEmployees_shouldReturnNull_whenServerReturnsNon2xxStatus() {
        // Given
        EmployeeServerResponse<List<EmployeeEntity>> serverResponse = new EmployeeServerResponse<>();
        serverResponse.setData(Collections.emptyList());
        serverResponse.setStatus("error");

        ResponseEntity<EmployeeServerResponse<List<EmployeeEntity>>> responseEntity =
                new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        List<Employee> result = employeeServerAdapter.loadAllEmployees();

        // Then
        assertThat(result).isNull();
    }

    @Test
    void loadAllEmployees_shouldReturnNull_whenRestTemplateThrowsException() {
        // Given
        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        List<Employee> result = employeeServerAdapter.loadAllEmployees();

        // Then
        assertThat(result).isNull();
    }

    // loadEmployeeById tests

    @Test
    void loadEmployeeById_shouldReturnEmployee_whenServerReturnsSuccessfulResponse() {
        // Given
        UUID employeeId = testEntity1.getId();
        EmployeeServerResponse<EmployeeEntity> serverResponse = new EmployeeServerResponse<>();
        serverResponse.setData(testEntity1);
        serverResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> responseEntity =
                new ResponseEntity<>(serverResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        when(employeeMapper.toEmployee(testEntity1)).thenReturn(testEmployee1);

        // When
        Employee result = employeeServerAdapter.loadEmployeeById(employeeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testEmployee1);
        verify(employeeMapper).toEmployee(testEntity1);
    }

    @Test
    void loadEmployeeById_shouldReturnNull_whenEmployeeNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + nonExistentId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When
        Employee result = employeeServerAdapter.loadEmployeeById(nonExistentId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void loadEmployeeById_shouldReturnNull_whenResponseBodyIsNull() {
        // Given
        UUID employeeId = UUID.randomUUID();
        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        Employee result = employeeServerAdapter.loadEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void loadEmployeeById_shouldReturnNull_whenServerReturnsNon2xxStatus() {
        // Given
        UUID employeeId = UUID.randomUUID();
        EmployeeServerResponse<EmployeeEntity> serverResponse = new EmployeeServerResponse<>();
        serverResponse.setData(null);
        serverResponse.setStatus("error");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> responseEntity =
                new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        Employee result = employeeServerAdapter.loadEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void loadEmployeeById_shouldReturnNull_whenServerThrowsBadRequestException() {
        // Given
        UUID employeeId = UUID.randomUUID();

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        Employee result = employeeServerAdapter.loadEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void loadEmployeeById_shouldReturnNull_whenServerThrowsUnauthorizedException() {
        // Given
        UUID employeeId = UUID.randomUUID();

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        Employee result = employeeServerAdapter.loadEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }

    // saveNewEmployee tests

    @Test
    void saveNewEmployee_shouldReturnCreatedEmployee_whenServerReturnsSuccessfulResponse() {
        // Given - Input does not include ID or email (server generates these)
        UUID newEmployeeId = UUID.randomUUID();
        Employee inputEmployee = Employee.builder()
                .name("New Employee")
                .salary(90000)
                .age(32)
                .title("Tech Lead")
                .build();

        EmployeeEntity responseEntity = new EmployeeEntity();
        responseEntity.setId(newEmployeeId);
        responseEntity.setEmployee_name("New Employee");
        responseEntity.setEmployee_salary(90000);
        responseEntity.setEmployee_age(32);
        responseEntity.setEmployee_title("Tech Lead");
        responseEntity.setEmployee_email("new.employee@example.com");

        Employee expectedEmployee = Employee.builder()
                .id(newEmployeeId)
                .name("New Employee")
                .salary(90000)
                .age(32)
                .title("Tech Lead")
                .email("new.employee@example.com")
                .build();

        EmployeeServerResponse<EmployeeEntity> serverResponse = new EmployeeServerResponse<>();
        serverResponse.setData(responseEntity);
        serverResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> response =
                new ResponseEntity<>(serverResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        when(employeeMapper.toEmployee(responseEntity)).thenReturn(expectedEmployee);

        // When
        Employee result = employeeServerAdapter.saveNewEmployee(inputEmployee);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedEmployee);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isNotNull();
        verify(employeeMapper).toEmployee(responseEntity);
    }

    @Test
    void saveNewEmployee_shouldReturnNull_whenResponseBodyIsNull() {
        // Given
        Employee inputEmployee =
                Employee.builder().name("New Employee").salary(90000).age(32).build();

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> response = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        // When
        Employee result = employeeServerAdapter.saveNewEmployee(inputEmployee);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void saveNewEmployee_shouldReturnNull_whenServerReturnsNon2xxStatus() {
        // Given
        Employee inputEmployee =
                Employee.builder().name("New Employee").salary(90000).age(32).build();

        EmployeeServerResponse<EmployeeEntity> serverResponse = new EmployeeServerResponse<>();
        serverResponse.setData(null);
        serverResponse.setStatus("error");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> response =
                new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        // When
        Employee result = employeeServerAdapter.saveNewEmployee(inputEmployee);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void saveNewEmployee_shouldReturnNull_whenRestTemplateThrowsException() {
        // Given
        Employee inputEmployee =
                Employee.builder().name("New Employee").salary(90000).age(32).build();

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        Employee result = employeeServerAdapter.saveNewEmployee(inputEmployee);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void saveNewEmployee_shouldReturnNull_whenServerThrowsUnauthorizedException() {
        // Given
        Employee inputEmployee =
                Employee.builder().name("New Employee").salary(90000).age(32).build();

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        Employee result = employeeServerAdapter.saveNewEmployee(inputEmployee);

        // Then
        assertThat(result).isNull();
    }

    // deleteEmployeeById tests

    @Test
    void deleteEmployeeById_shouldReturnEmployeeName_whenEmployeeIsDeletedSuccessfully() {
        // Given
        UUID employeeId = UUID.randomUUID();

        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setId(employeeId);
        employeeEntity.setEmployee_name("John Doe");
        employeeEntity.setEmployee_salary(75000);
        employeeEntity.setEmployee_age(30);
        employeeEntity.setEmployee_title("Software Engineer");
        employeeEntity.setEmployee_email("john.doe@example.com");

        Employee employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@example.com")
                .build();

        EmployeeServerResponse<EmployeeEntity> getServerResponse = new EmployeeServerResponse<>();
        getServerResponse.setData(employeeEntity);
        getServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> getResponseEntity =
                new ResponseEntity<>(getServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);

        when(employeeMapper.toEmployee(employeeEntity)).thenReturn(employee);

        EmployeeServerResponse<Boolean> deleteServerResponse = new EmployeeServerResponse<>();
        deleteServerResponse.setData(true);
        deleteServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<Boolean>> deleteResponseEntity =
                new ResponseEntity<>(deleteServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);

        // When
        String result = employeeServerAdapter.deleteEmployeeById(employeeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("John Doe");
    }

    @Test
    void deleteEmployeeById_shouldReturnNull_whenEmployeeNotFound() {
        // Given
        UUID employeeId = UUID.randomUUID();

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When
        String result = employeeServerAdapter.deleteEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void deleteEmployeeById_shouldReturnNull_whenDeleteOperationFails() {
        // Given
        UUID employeeId = UUID.randomUUID();

        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setId(employeeId);
        employeeEntity.setEmployee_name("John Doe");

        Employee employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .build();

        EmployeeServerResponse<EmployeeEntity> getServerResponse = new EmployeeServerResponse<>();
        getServerResponse.setData(employeeEntity);
        getServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> getResponseEntity =
                new ResponseEntity<>(getServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);

        when(employeeMapper.toEmployee(employeeEntity)).thenReturn(employee);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String result = employeeServerAdapter.deleteEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void deleteEmployeeById_shouldReturnNull_whenDeleteResponseReturnsFalse() {
        // Given
        UUID employeeId = UUID.randomUUID();

        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setId(employeeId);
        employeeEntity.setEmployee_name("John Doe");

        Employee employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .build();

        EmployeeServerResponse<EmployeeEntity> getServerResponse = new EmployeeServerResponse<>();
        getServerResponse.setData(employeeEntity);
        getServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> getResponseEntity =
                new ResponseEntity<>(getServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);

        when(employeeMapper.toEmployee(employeeEntity)).thenReturn(employee);

        EmployeeServerResponse<Boolean> deleteServerResponse = new EmployeeServerResponse<>();
        deleteServerResponse.setData(false);
        deleteServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<Boolean>> deleteResponseEntity =
                new ResponseEntity<>(deleteServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);

        // When
        String result = employeeServerAdapter.deleteEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void deleteEmployeeById_shouldReturnNull_whenDeleteResponseBodyIsNull() {
        // Given
        UUID employeeId = UUID.randomUUID();

        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setId(employeeId);
        employeeEntity.setEmployee_name("John Doe");

        Employee employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .build();

        EmployeeServerResponse<EmployeeEntity> getServerResponse = new EmployeeServerResponse<>();
        getServerResponse.setData(employeeEntity);
        getServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> getResponseEntity =
                new ResponseEntity<>(getServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);

        when(employeeMapper.toEmployee(employeeEntity)).thenReturn(employee);

        ResponseEntity<EmployeeServerResponse<Boolean>> deleteResponseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);

        // When
        String result = employeeServerAdapter.deleteEmployeeById(employeeId);

        // Then
        assertThat(result).isNull();
    }
}
