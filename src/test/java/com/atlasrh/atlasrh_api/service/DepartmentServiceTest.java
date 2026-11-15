package com.atlasrh.atlasrh_api.service;

import com.atlasrh.atlasrh_api.dto.DepartmentDTO;
import com.atlasrh.atlasrh_api.dto.DepartmentRequestDTO;
import com.atlasrh.atlasrh_api.entity.Department;
import com.atlasrh.atlasrh_api.entity.Employee;
import com.atlasrh.atlasrh_api.exception.ConflictException;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import com.atlasrh.atlasrh_api.mapper.DepartmentMapper;
import com.atlasrh.atlasrh_api.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                .employees(new ArrayList<>())
                .build();

        departmentDTO = DepartmentDTO.builder()
                .id(1L)
                .name("IT")
                .description("Service informatique")
                .build();
    }

    // Test de creation de departement lorsque nom nexiste pas encore
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

    // Test de creation de departement quand son nom existe
    @Test
    void shouldThrowConflictExceptionWhenNameAlreadyExists() {
        when(departmentRepository.existsByName("IT")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("existe déjà");

        verify(departmentRepository, never()).save(any());
    }

    // Teste de recuperation de departement par son id quand il existe
    @Test
    void shouldReturnDepartmentWhenIdExists() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentMapper.toDto(department)).thenReturn(departmentDTO);

        DepartmentDTO result = departmentService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("IT");
    }

    // Teste la recuperation de departement par un id inexistant
    @Test
    void shouldThrowNotFoundExceptionWhenIdDoesNotExist() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("introuvable");
    }

    // Teste la récupération de tous les départements -> doit retourner la liste convertie en DTO
    @Test
    void shouldReturnAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));
        when(departmentMapper.toDto(department)).thenReturn(departmentDTO);

        List<DepartmentDTO> result = departmentService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("IT");
    }

    // Teste la modification d'un département quand le nouveau nom est disponible -> doit réussir et appeler save()
    @Test
    void shouldUpdateDepartmentWhenNameIsAvailable() {
        DepartmentRequestDTO updateRequest = DepartmentRequestDTO.builder()
                .name("IT Updated")
                .description("Nouvelle description")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.findByName("IT Updated")).thenReturn(Optional.empty());
        when(departmentRepository.save(department)).thenReturn(department);
        when(departmentMapper.toDto(department)).thenReturn(departmentDTO);

        DepartmentDTO result = departmentService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(departmentRepository, times(1)).save(department);
    }

    // Teste la modification d'un département vers un nom déjà utilisé par un AUTRE département -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenUpdatingToExistingNameOfAnotherDepartment() {
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

        assertThatThrownBy(() -> departmentService.update(1L, updateRequest))
                .isInstanceOf(ConflictException.class);

        verify(departmentRepository, never()).save(any());
    }

    // Teste la suppression d'un département qui n'a aucun employé -> doit réussir et appeler delete()
    @Test
    void shouldDeleteDepartmentWhenItHasNoEmployees() {
        department.setEmployees(new ArrayList<>());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        departmentService.delete(1L);

        verify(departmentRepository, times(1)).delete(department);
    }

    // Teste la suppression d'un département qui contient au moins un employé -> doit lever ConflictException et ne jamais supprimer
    @Test
    void shouldThrowConflictWhenDeletingDepartmentWithEmployees() {
        Employee employee = Employee.builder().id(1L).build();
        department.setEmployees(List.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("employés");

        verify(departmentRepository, never()).delete(any());
    }
}