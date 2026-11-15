package com.atlasrh.atlasrh_api.service;

import com.atlasrh.atlasrh_api.dto.DepartmentDTO;
import com.atlasrh.atlasrh_api.dto.DepartmentRequestDTO;
import com.atlasrh.atlasrh_api.entity.Department;
import com.atlasrh.atlasrh_api.mapper.DepartmentMapper;
import com.atlasrh.atlasrh_api.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    private DepartmentRequestDTO requestDTO;
    private Department department;
    private DepartmentDTO departmentDTO;

    @BeforeEach
    void setUp() {
        requestDTO = DepartmentRequestDTO.builder()
                .name("IT")
                .description("Service informatique")
                .build();

        department = Department.builder()
                .id(1L)
                .name("IT")
                .description("Service informatique")
                .build();

        departmentDTO = DepartmentDTO.builder()
                .id(1L)
                .name("IT")
                .description("Service informatique")
                .build();
    }

    @Test
    void shouldCreateDepartmentWhenNameDoesNotExist() {
        // Given
        when(departmentRepository.existsByName("IT")).thenReturn(false);
        when(departmentMapper.toEntity(requestDTO)).thenReturn(department);
        when(departmentRepository.save(department)).thenReturn(department);
        when(departmentMapper.toDto(department)).thenReturn(departmentDTO);

        // When
        DepartmentDTO result = departmentService.create(requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("IT");
        verify(departmentRepository, times(1)).save(department);
    }
}