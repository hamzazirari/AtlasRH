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
import com.atlasrh.atlasrh_api.exception.ConflictException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import java.util.Optional;
import com.atlasrh.atlasrh_api.entity.Employee;
import java.util.ArrayList;
import java.util.List;

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

        when(departmentRepository.existsByName("IT")).thenReturn(false);
        when(departmentMapper.toEntity(requestDTO)).thenReturn(department);
        when(departmentRepository.save(department)).thenReturn(department);
        when(departmentMapper.toDto(department)).thenReturn(departmentDTO);

        DepartmentDTO result = departmentService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("IT");
        verify(departmentRepository, times(1)).save(department);
    }

    @Test
    void shouldThrowConflictExceptionWhenNameAlreadyExists() {
        when(departmentRepository.existsByName("IT")).thenReturn(true);


        assertThatThrownBy(() -> departmentService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("existe déjà");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void shouldReturnDepartmentWhenIdExists() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentMapper.toDto(department)).thenReturn(departmentDTO);

        // When
        DepartmentDTO result = departmentService.getById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("IT");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenIdDoesNotExist() {
        // Given
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }


    @Test
    void shouldUpdateDepartmentWhenNameIsAvailable() {
        // Given
        DepartmentRequestDTO updateRequest = DepartmentRequestDTO.builder()
                .name("IT Updated")
                .description("Nouvelle description")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.findByName("IT Updated")).thenReturn(Optional.empty());
        when(departmentRepository.save(department)).thenReturn(department);
        when(departmentMapper.toDto(department)).thenReturn(departmentDTO);

        // When
        DepartmentDTO result = departmentService.update(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(departmentRepository, times(1)).save(department);
    }

    @Test
    void shouldThrowConflictWhenUpdatingToExistingNameOfAnotherDepartment() {
        // Given
        DepartmentRequestDTO updateRequest = DepartmentRequestDTO.builder()
                .name("RH")
                .description("desc")
                .build();

        Department anotherDepartment = Department.builder()
                .id(2L)
                .name("RH")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.findByName("RH")).thenReturn(Optional.of(anotherDepartment));

        // When / Then
        assertThatThrownBy(() -> departmentService.update(1L, updateRequest))
                .isInstanceOf(ConflictException.class);

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteDepartmentWhenItHasNoEmployees() {
        // Given
        department.setEmployees(new ArrayList<>());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        // When
        departmentService.delete(1L);

        // Then
        verify(departmentRepository, times(1)).delete(department);
    }

    @Test
    void shouldThrowConflictWhenDeletingDepartmentWithEmployees() {
        // Given
        Employee employee = Employee.builder().id(1L).build();
        department.setEmployees(List.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        // When / Then
        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("employés");

        verify(departmentRepository, never()).delete(any());
    }
}