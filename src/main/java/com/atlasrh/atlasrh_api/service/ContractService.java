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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractMapper contractMapper;

    @Transactional
    public ContractDTO create(ContractRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'id : " + requestDTO.getEmployeeId()));

        if ((requestDTO.getType() == ContractType.CDD || requestDTO.getType() == ContractType.STAGE)
                && requestDTO.getEndDate() == null) {
            throw new ConflictException("Un contrat CDD ou STAGE doit avoir une date de fin");
        }

        if (requestDTO.getEndDate() != null && requestDTO.getEndDate().isBefore(requestDTO.getStartDate())) {
            throw new ConflictException("La date de fin ne peut pas être avant la date de début");
        }

        boolean hasOngoingContract = contractRepository
                .findByEmployeeIdAndEndDateIsNullOrEndDateGreaterThanEqual(requestDTO.getEmployeeId(), LocalDate.now())
                .isPresent();
        if (hasOngoingContract) {
            throw new ConflictException("Cet employé a déjà un contrat en cours");
        }

        Contract contract = Contract.builder()
                .type(requestDTO.getType())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .grossSalary(requestDTO.getGrossSalary())
                .employee(employee)
                .build();

        Contract saved = contractRepository.save(contract);
        return contractMapper.toDto(saved);
    }

    @Transactional
    public ContractDTO terminate(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable avec l'id : " + id));

        if (contract.getEndDate() != null && contract.getEndDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Ce contrat est déjà terminé");
        }

        contract.setEndDate(LocalDate.now());
        Contract updated = contractRepository.save(contract);
        return contractMapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public List<ContractDTO> getHistory(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'id : " + employeeId));
        return contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId).stream()
                .map(contractMapper::toDto)
                .collect(Collectors.toList());
    }
}