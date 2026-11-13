package com.atlasrh.atlasrh_api.service;

import com.atlasrh.atlasrh_api.dto.LeaveDecisionDTO;
import com.atlasrh.atlasrh_api.dto.LeaveRequestDTO;
import com.atlasrh.atlasrh_api.entity.LeaveRequest;
import com.atlasrh.atlasrh_api.enums.LeaveStatus;
import com.atlasrh.atlasrh_api.exception.ConflictException;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import com.atlasrh.atlasrh_api.mapper.LeaveRequestMapper;
import com.atlasrh.atlasrh_api.repository.EmployeeRepository;
import com.atlasrh.atlasrh_api.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getPendingForManager(Long managerId) {
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager introuvable avec l'id : " + managerId));

        return leaveRequestRepository.findByEmployeeManagerIdAndStatus(managerId, LeaveStatus.EN_ATTENTE).stream()
                .map(this::toDtoWithWorkingDays)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestDTO approve(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'id : " + requestId));

        if (request.getStatus() != LeaveStatus.EN_ATTENTE) {
            throw new ConflictException("Seule une demande EN_ATTENTE peut être traitée");
        }

        request.setStatus(LeaveStatus.APPROUVEE);
        return toDtoWithWorkingDays(leaveRequestRepository.save(request));
    }

    @Transactional
    public LeaveRequestDTO refuse(Long requestId, LeaveDecisionDTO decisionDTO) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'id : " + requestId));

        if (request.getStatus() != LeaveStatus.EN_ATTENTE) {
            throw new ConflictException("Seule une demande EN_ATTENTE peut être traitée");
        }
        if (!StringUtils.hasText(decisionDTO.getComment())) {
            throw new ConflictException("Un commentaire est obligatoire pour refuser une demande");
        }

        request.setStatus(LeaveStatus.REFUSEE);
        request.setComment(decisionDTO.getComment());
        return toDtoWithWorkingDays(leaveRequestRepository.save(request));
    }

    private LeaveRequestDTO toDtoWithWorkingDays(LeaveRequest request) {
        LeaveRequestDTO dto = leaveRequestMapper.toDto(request);
        dto.setWorkingDays(calculateWorkingDays(request.getStartDate(), request.getEndDate()));
        return dto;
    }

    public static int calculateWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }
}