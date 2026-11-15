package com.atlasrh.atlasrh_api.service;

import com.atlasrh.atlasrh_api.dto.ContractDTO;
import com.atlasrh.atlasrh_api.dto.ContractRequestDTO;
import com.atlasrh.atlasrh_api.entity.Contract;
import com.atlasrh.atlasrh_api.entity.Employee;
import com.atlasrh.atlasrh_api.enums.ContractType;
import com.atlasrh.atlasrh_api.exception.ConflictException;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import com.atlasrh.atlasrh_api.mapper.ContractMapper;
import com.atlasrh.atlasrh_api.repository.ContractRepository;
import com.atlasrh.atlasrh_api.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private ContractService contractService;

    private Employee employee;
    private ContractRequestDTO requestDTO;
    private Contract contract;
    private ContractDTO contractDTO;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .build();

        requestDTO = ContractRequestDTO.builder()
                .type(ContractType.CDI)
                .startDate(LocalDate.of(2023, 1, 15))
                .endDate(null)
                .grossSalary(BigDecimal.valueOf(3500))
                .employeeId(1L)
                .build();

        contract = Contract.builder()
                .id(1L)
                .type(ContractType.CDI)
                .startDate(LocalDate.of(2023, 1, 15))
                .grossSalary(BigDecimal.valueOf(3500))
                .employee(employee)
                .build();

        contractDTO = ContractDTO.builder()
                .id(1L)
                .type(ContractType.CDI)
                .build();
    }

    // Teste la création d'un contrat CDI valide, sans contrat en cours existant -> doit réussir
    @Test
    void shouldCreateContractWhenDataIsValid() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.findByEmployeeIdAndEndDateIsNullOrEndDateGreaterThanEqual(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);
        when(contractMapper.toDto(contract)).thenReturn(contractDTO);

        ContractDTO result = contractService.create(requestDTO);

        assertThat(result).isNotNull();
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    // Teste la création d'un contrat CDD SANS date de fin -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenCddHasNoEndDate() {
        requestDTO.setType(ContractType.CDD);
        requestDTO.setEndDate(null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> contractService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("date de fin");

        verify(contractRepository, never()).save(any());
    }

    // Teste la création d'un contrat STAGE SANS date de fin -> doit lever ConflictException (même règle que CDD)
    @Test
    void shouldThrowConflictWhenStageHasNoEndDate() {
        requestDTO.setType(ContractType.STAGE);
        requestDTO.setEndDate(null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> contractService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("date de fin");
    }

    // Teste la création d'un contrat où la date de fin est AVANT la date de début -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenEndDateBeforeStartDate() {
        requestDTO.setType(ContractType.CDD);
        requestDTO.setStartDate(LocalDate.of(2024, 6, 1));
        requestDTO.setEndDate(LocalDate.of(2024, 1, 1)); // avant le début !

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> contractService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("date de fin ne peut pas");
    }

    // Teste la création d'un contrat pour un employé qui a DÉJÀ un contrat en cours -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenEmployeeAlreadyHasOngoingContract() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.findByEmployeeIdAndEndDateIsNullOrEndDateGreaterThanEqual(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(contract)); // un contrat en cours existe déjà

        assertThatThrownBy(() -> contractService.create(requestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("contrat en cours");

        verify(contractRepository, never()).save(any());
    }

    // Teste la création d'un contrat pour un employé qui n'existe pas -> doit lever ResourceNotFoundException
    @Test
    void shouldThrowNotFoundWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        requestDTO.setEmployeeId(99L);

        assertThatThrownBy(() -> contractService.create(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Teste la terminaison d'un contrat en cours -> doit mettre à jour sa endDate à aujourd'hui
    @Test
    void shouldTerminateOngoingContract() {
        Contract ongoingContract = Contract.builder()
                .id(1L)
                .type(ContractType.CDI)
                .startDate(LocalDate.of(2023, 1, 1))
                .endDate(null)
                .employee(employee)
                .build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(ongoingContract));
        when(contractRepository.save(any(Contract.class))).thenReturn(ongoingContract);
        when(contractMapper.toDto(any(Contract.class))).thenReturn(contractDTO);

        ContractDTO result = contractService.terminate(1L);

        assertThat(result).isNotNull();
        assertThat(ongoingContract.getEndDate()).isEqualTo(LocalDate.now());
    }

    // Teste la terminaison d'un contrat déjà terminé -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenTerminatingAlreadyTerminatedContract() {
        Contract terminatedContract = Contract.builder()
                .id(1L)
                .endDate(LocalDate.now().minusDays(10))
                .build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(terminatedContract));

        assertThatThrownBy(() -> contractService.terminate(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("déjà terminé");
    }
}