package com.atlasrh.atlasrh_api.service;

import com.atlasrh.atlasrh_api.dto.DepartmentDTO;
import com.atlasrh.atlasrh_api.dto.DepartmentRequestDTO;
import com.atlasrh.atlasrh_api.entity.Department;
import com.atlasrh.atlasrh_api.exception.ConflictException;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import com.atlasrh.atlasrh_api.mapper.DepartmentMapper;
import com.atlasrh.atlasrh_api.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentDTO create(DepartmentRequestDTO requestDTO) {
        if (departmentRepository.existsByName(requestDTO.getName())) {
            throw new ConflictException("Un département avec ce nom existe déjà : " + requestDTO.getName());
        }
        Department department = departmentMapper.toEntity(requestDTO);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public DepartmentDTO getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'id : " + id));
        return departmentMapper.toDto(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAll() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDTO update(Long id, DepartmentRequestDTO requestDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'id : " + id));

        departmentRepository.findByName(requestDTO.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Un département avec ce nom existe déjà : " + requestDTO.getName());
            }
        });

        department.setName(requestDTO.getName());
        department.setDescription(requestDTO.getDescription());
        Department updated = departmentRepository.save(department);
        return departmentMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'id : " + id));

        if (!department.getEmployees().isEmpty()) {
            throw new ConflictException("Impossible de supprimer un département qui contient des employés");
        }
        departmentRepository.delete(department);
    }
}