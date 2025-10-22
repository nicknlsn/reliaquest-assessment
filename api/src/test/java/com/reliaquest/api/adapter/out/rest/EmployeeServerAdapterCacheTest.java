package com.reliaquest.api.adapter.out.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.application.domain.model.Employee;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for EmployeeServerAdapter caching behavior.
 * These tests verify that Caffeine caching is working correctly.
 */
@SpringBootTest
class EmployeeServerAdapterCacheTest {

    @Autowired
    private EmployeeServerAdapter employeeServerAdapter;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private EmployeeMapper employeeMapper;

    private EmployeeEntity testEntity1;
    private EmployeeEntity testEntity2;
    private Employee testEmployee1;
    private Employee testEmployee2;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

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

    @Test
    void loadAllEmployees_shouldCacheResults_whenCalledMultipleTimes() {
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

        // When - Call the method multiple times
        List<Employee> result1 = employeeServerAdapter.loadAllEmployees();
        List<Employee> result2 = employeeServerAdapter.loadAllEmployees();
        List<Employee> result3 = employeeServerAdapter.loadAllEmployees();

        // Then - RestTemplate should only be called once due to caching
        verify(restTemplate, times(1))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));

        // All results should be the same
        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(2);
        assertThat(result3).hasSize(2);
        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
    }

    @Test
    void loadEmployeeById_shouldCacheResults_whenCalledWithSameId() {
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

        // When - Call the method multiple times with the same ID
        Employee result1 = employeeServerAdapter.loadEmployeeById(employeeId);
        Employee result2 = employeeServerAdapter.loadEmployeeById(employeeId);
        Employee result3 = employeeServerAdapter.loadEmployeeById(employeeId);

        // Then - RestTemplate should only be called once due to caching
        verify(restTemplate, times(1))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));

        // All results should be the same
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();
        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
    }

    @Test
    void loadEmployeeById_shouldUseDifferentCacheEntries_forDifferentIds() {
        // Given
        UUID employeeId1 = testEntity1.getId();
        UUID employeeId2 = testEntity2.getId();

        EmployeeServerResponse<EmployeeEntity> serverResponse1 = new EmployeeServerResponse<>();
        serverResponse1.setData(testEntity1);
        serverResponse1.setStatus("success");

        EmployeeServerResponse<EmployeeEntity> serverResponse2 = new EmployeeServerResponse<>();
        serverResponse2.setData(testEntity2);
        serverResponse2.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> responseEntity1 =
                new ResponseEntity<>(serverResponse1, HttpStatus.OK);
        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> responseEntity2 =
                new ResponseEntity<>(serverResponse2, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId1),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity1);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId2),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity2);

        when(employeeMapper.toEmployee(testEntity1)).thenReturn(testEmployee1);
        when(employeeMapper.toEmployee(testEntity2)).thenReturn(testEmployee2);

        // When - Call with different IDs
        Employee result1 = employeeServerAdapter.loadEmployeeById(employeeId1);
        Employee result2 = employeeServerAdapter.loadEmployeeById(employeeId2);
        Employee result3 = employeeServerAdapter.loadEmployeeById(employeeId1); // Cached
        Employee result4 = employeeServerAdapter.loadEmployeeById(employeeId2); // Cached

        // Then - Each unique ID should call RestTemplate once
        verify(restTemplate, times(1))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId1),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));

        verify(restTemplate, times(1))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId2),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));

        // Results should match appropriately
        assertThat(result1).isEqualTo(testEmployee1);
        assertThat(result2).isEqualTo(testEmployee2);
        assertThat(result3).isEqualTo(testEmployee1); // From cache
        assertThat(result4).isEqualTo(testEmployee2); // From cache
    }

    @Test
    void cacheManager_shouldHaveCorrectCachesConfigured() {
        // When
        var cacheNames = cacheManager.getCacheNames();

        // Then - Verify caches are registered (they get created on first use or can be pre-configured)
        // At minimum, verify CacheManager exists and can create caches
        assertThat(cacheManager).isNotNull();

        // Trigger cache creation by calling methods
        setupAndCallLoadAllEmployees();
        setupAndCallLoadEmployeeById();

        // Verify caches now exist
        var allEmployeesCache = cacheManager.getCache("allEmployees");
        var employeeByIdCache = cacheManager.getCache("employeeById");

        assertThat(allEmployeesCache).isNotNull();
        assertThat(employeeByIdCache).isNotNull();
    }

    @Test
    void cache_shouldBeCleared_whenCacheManagerEvictsCache() {
        // Given
        UUID employeeId = testEntity1.getId();
        setupLoadEmployeeByIdMock(employeeId);

        // When - First call (not cached)
        Employee result1 = employeeServerAdapter.loadEmployeeById(employeeId);

        // Clear the cache
        var cache = cacheManager.getCache("employeeById");
        assertThat(cache).isNotNull();
        cache.clear();

        // Second call (should hit the service again after cache clear)
        Employee result2 = employeeServerAdapter.loadEmployeeById(employeeId);

        // Then - RestTemplate should be called twice (once before clear, once after)
        verify(restTemplate, times(2))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));

        assertThat(result1).isEqualTo(testEmployee1);
        assertThat(result2).isEqualTo(testEmployee1);
    }

    // Helper methods

    private void setupAndCallLoadAllEmployees() {
        List<EmployeeEntity> entities = Arrays.asList(testEntity1);
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

        when(employeeMapper.toEmployee(any())).thenReturn(testEmployee1);

        employeeServerAdapter.loadAllEmployees();
    }

    private void setupAndCallLoadEmployeeById() {
        UUID employeeId = testEntity1.getId();
        setupLoadEmployeeByIdMock(employeeId);
        employeeServerAdapter.loadEmployeeById(employeeId);
    }

    private void setupLoadEmployeeByIdMock(UUID employeeId) {
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
    }

    // saveNewEmployee cache eviction tests

    @Test
    void saveNewEmployee_shouldEvictAllEmployeesCache_whenEmployeeIsCreated() {
        // Given - First populate the allEmployees cache
        List<EmployeeEntity> entities = Arrays.asList(testEntity1);
        EmployeeServerResponse<List<EmployeeEntity>> listServerResponse = new EmployeeServerResponse<>();
        listServerResponse.setData(entities);
        listServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<List<EmployeeEntity>>> listResponseEntity =
                new ResponseEntity<>(listServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(listResponseEntity);

        when(employeeMapper.toEmployee(testEntity1)).thenReturn(testEmployee1);

        // Populate the cache
        employeeServerAdapter.loadAllEmployees();

        // Verify cache was populated (only 1 call so far)
        verify(restTemplate, times(1))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));

        // Setup for saveNewEmployee
        UUID newEmployeeId = UUID.randomUUID();
        Employee inputEmployee = Employee.builder()
                .name("New Employee")
                .salary(90000)
                .age(32)
                .title("Tech Lead")
                .build();

        EmployeeEntity newEmployeeEntity = new EmployeeEntity();
        newEmployeeEntity.setId(newEmployeeId);
        newEmployeeEntity.setEmployee_name("New Employee");
        newEmployeeEntity.setEmployee_salary(90000);
        newEmployeeEntity.setEmployee_age(32);
        newEmployeeEntity.setEmployee_title("Tech Lead");
        newEmployeeEntity.setEmployee_email("new.employee@example.com");

        Employee createdEmployee = Employee.builder()
                .id(newEmployeeId)
                .name("New Employee")
                .salary(90000)
                .age(32)
                .title("Tech Lead")
                .email("new.employee@example.com")
                .build();

        EmployeeServerResponse<EmployeeEntity> createServerResponse = new EmployeeServerResponse<>();
        createServerResponse.setData(newEmployeeEntity);
        createServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> createResponseEntity =
                new ResponseEntity<>(createServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(createResponseEntity);

        when(employeeMapper.toEmployee(newEmployeeEntity)).thenReturn(createdEmployee);

        // When - Create a new employee (should evict allEmployees cache)
        Employee result = employeeServerAdapter.saveNewEmployee(inputEmployee);

        // Then - Verify employee was created
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(newEmployeeId);

        // Now call loadAllEmployees again - it should hit the REST API again because cache was evicted
        employeeServerAdapter.loadAllEmployees();

        // Verify RestTemplate was called twice for GET all employees (once before, once after cache eviction)
        verify(restTemplate, times(2))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void saveNewEmployee_shouldNotAffectEmployeeByIdCache_whenEmployeeIsCreated() {
        // Given - First populate the employeeById cache
        UUID employeeId = testEntity1.getId();
        setupLoadEmployeeByIdMock(employeeId);

        // Populate the cache
        employeeServerAdapter.loadEmployeeById(employeeId);

        // Verify cache was populated (only 1 call so far)
        verify(restTemplate, times(1))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));

        // Setup for saveNewEmployee
        UUID newEmployeeId = UUID.randomUUID();
        Employee inputEmployee = Employee.builder()
                .name("New Employee")
                .salary(90000)
                .age(32)
                .build();

        EmployeeEntity newEmployeeEntity = new EmployeeEntity();
        newEmployeeEntity.setId(newEmployeeId);
        newEmployeeEntity.setEmployee_name("New Employee");
        newEmployeeEntity.setEmployee_salary(90000);
        newEmployeeEntity.setEmployee_age(32);
        newEmployeeEntity.setEmployee_email("new.employee@example.com");

        Employee createdEmployee = Employee.builder()
                .id(newEmployeeId)
                .name("New Employee")
                .salary(90000)
                .age(32)
                .email("new.employee@example.com")
                .build();

        EmployeeServerResponse<EmployeeEntity> createServerResponse = new EmployeeServerResponse<>();
        createServerResponse.setData(newEmployeeEntity);
        createServerResponse.setStatus("success");

        ResponseEntity<EmployeeServerResponse<EmployeeEntity>> createResponseEntity =
                new ResponseEntity<>(createServerResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(createResponseEntity);

        when(employeeMapper.toEmployee(newEmployeeEntity)).thenReturn(createdEmployee);

        // When - Create a new employee
        Employee result = employeeServerAdapter.saveNewEmployee(inputEmployee);

        // Then - Verify employee was created
        assertThat(result).isNotNull();

        // Call loadEmployeeById again with the same ID - should still use cache
        employeeServerAdapter.loadEmployeeById(employeeId);

        // Verify RestTemplate was still only called once for the specific employee ID
        // (cache should NOT have been evicted for employeeById)
        verify(restTemplate, times(1))
                .exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        eq(null),
                        any(ParameterizedTypeReference.class));
    }
}
