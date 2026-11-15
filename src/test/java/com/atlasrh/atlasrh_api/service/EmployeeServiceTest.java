package com.atlasrh.atlasrh_api.service;

import com.atlasrh.atlasrh_api.dto.EmployeeDTO;
import com.atlasrh.atlasrh_api.dto.EmployeeRequestDTO;
import com.atlasrh.atlasrh_api.entity.Department;
import com.atlasrh.atlasrh_api.entity.Employee;
import com.atlasrh.atlasrh_api.exception.ConflictException;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import com.atlasrh.atlasrh_api.mapper.EmployeeMapper;
import com.atlasrh.atlasrh_api.repository.DepartmentRepository;
import com.atlasrh.atlasrh_api.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeRequestDTO requestDTO;
    private Employee employee;
    private EmployeeDTO employeeDTO;
    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("IT")
                .build();

        requestDTO = EmployeeRequestDTO.builder()
                .matricule("EMP001")
                .lastName("Dupont")
                .firstName("Jean")
                .email("jean.dupont@atlasrh.com")
                .jobTitle("Développeur")
                .hireDate(LocalDate.of(2023, 1, 15))
                .departmentId(1L)
                .managerId(null)
                .build();

        employee = Employee.builder()
                .id(1L)
                .matricule("EMP001")
                .lastName("Dupont")
                .firstName("Jean")
                .email("jean.dupont@atlasrh.com")
                .jobTitle("Développeur")
                .hireDate(LocalDate.of(2023, 1, 15))
                .department(department)
                .build();

        employeeDTO = EmployeeDTO.builder()
                .id(1L)
                .matricule("EMP001")
                .lastName("Dupont")
                .firstName("Jean")
                .build();
    }

    // Teste la création d'un employé avec matricule/email uniques et département existant -> doit réussir
    @Test
    void shouldCreateEmployeeWhenDataIsValid() {
        when(employeeRepository.existsByMatricule("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("jean.dupont@atlasrh.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getMatricule()).isEqualTo("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    // Teste la création d'un employé avec un matricule déjà utilisé -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenMatriculeAlreadyExists() {
        when(employeeRepository.existsByMatricule("EMP001")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("matricule");

        verify(employeeRepository, never()).save(any());
    }

    // Teste la création d'un employé avec un email déjà utilisé -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        when(employeeRepository.existsByMatricule("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("jean.dupont@atlasrh.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");

        verify(employeeRepository, never()).save(any());
    }

    // Teste la création d'un employé avec un departmentId qui n'existe pas -> doit lever ResourceNotFoundException
    @Test
    void shouldThrowNotFoundWhenDepartmentDoesNotExist() {
        when(employeeRepository.existsByMatricule("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("jean.dupont@atlasrh.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.create(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Département");

        verify(employeeRepository, never()).save(any());
    }

    // Teste la récupération d'un employé par son id quand il existe -> doit retourner le bon DTO
    @Test
    void shouldReturnEmployeeWhenIdExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Jean");
    }

    // Teste la récupération d'un employé par un id inexistant -> doit lever ResourceNotFoundException
    @Test
    void shouldThrowNotFoundWhenEmployeeIdDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Teste la modification d'un employé en essayant de se définir comme son propre manager -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenEmployeeIsOwnManager() {
        EmployeeRequestDTO updateRequest = EmployeeRequestDTO.builder()
                .matricule("EMP001")
                .lastName("Dupont")
                .firstName("Jean")
                .email("jean.dupont@atlasrh.com")
                .jobTitle("Développeur")
                .hireDate(LocalDate.of(2023, 1, 15))
                .departmentId(1L)
                .managerId(1L) // même id que l'employé modifié (id=1)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByMatricule("EMP001")).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        assertThatThrownBy(() -> employeeService.update(1L, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("propre manager");

        verify(employeeRepository, never()).save(any());
    }

    // Teste la récupération de l'équipe d'un manager -> doit retourner la liste de ses subordonnés
    @Test
    void shouldReturnTeamMembersOfManager() {
        Employee teamMember = Employee.builder().id(2L).firstName("Sophie").lastName("Martin").build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByManagerId(1L)).thenReturn(List.of(teamMember));
        when(employeeMapper.toDto(teamMember)).thenReturn(
                EmployeeDTO.builder().id(2L).firstName("Sophie").lastName("Martin").build());

        List<EmployeeDTO> result = employeeService.getTeam(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Sophie");
    }

    // Teste la récupération de l'équipe d'un manager qui n'existe pas -> doit lever ResourceNotFoundException
    @Test
    void shouldThrowNotFoundWhenManagerDoesNotExistForTeam() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getTeam(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}