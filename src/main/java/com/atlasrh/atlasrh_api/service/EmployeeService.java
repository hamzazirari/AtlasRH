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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeDTO create(EmployeeRequestDTO requestDTO) {
        if (employeeRepository.existsByMatricule(requestDTO.getMatricule())) {
            throw new ConflictException("Un employé avec ce matricule existe déjà : " + requestDTO.getMatricule());
        }
        if (employeeRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ConflictException("Un employé avec cet email existe déjà : " + requestDTO.getEmail());
        }

        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'id : " + requestDTO.getDepartmentId()));

        Employee manager = null;
        if (requestDTO.getManagerId() != null) {
            manager = employeeRepository.findById(requestDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager introuvable avec l'id : " + requestDTO.getManagerId()));
        }

        Employee employee = Employee.builder()
                .matricule(requestDTO.getMatricule())
                .lastName(requestDTO.getLastName())
                .firstName(requestDTO.getFirstName())
                .email(requestDTO.getEmail())
                .jobTitle(requestDTO.getJobTitle())
                .hireDate(requestDTO.getHireDate())
                .department(department)
                .manager(manager)
                .build();

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'id : " + id));
        return employeeMapper.toDto(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> search(String name, Long departmentId, Pageable pageable) {
        return employeeRepository.search(name, departmentId, pageable)
                .map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getTeam(Long managerId) {
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager introuvable avec l'id : " + managerId));
        return employeeRepository.findByManagerId(managerId).stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDTO update(Long id, EmployeeRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'id : " + id));

        employeeRepository.findByMatricule(requestDTO.getMatricule()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Un employé avec ce matricule existe déjà : " + requestDTO.getMatricule());
            }
        });

        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'id : " + requestDTO.getDepartmentId()));

        Employee manager = null;
        if (requestDTO.getManagerId() != null) {
            if (requestDTO.getManagerId().equals(id)) {
                throw new ConflictException("Un employé ne peut pas être son propre manager");
            }
            manager = employeeRepository.findById(requestDTO.getMa