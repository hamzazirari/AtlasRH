package com.atlasrh.atlasrh_api.controller;

import com.atlasrh.atlasrh_api.dto.LeaveDecisionDTO;
import com.atlasrh.atlasrh_api.dto.LeaveRequestDTO;
import com.atlasrh.atlasrh_api.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping("/manager/{managerId}/pending")
    public ResponseEntity<List<LeaveRequestDTO>> getPendingForManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(leaveRequestService.getPendingForManager(managerId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.approve(id));
    }

    @PutMapping("/{id}/refuse")
    public ResponseEntity<LeaveRequestDTO> refuse(@PathVariable Long id, @Valid @RequestBody LeaveDecisionDTO decisionDTO) {
        return ResponseEntity.ok(leaveRequestService.refuse(id, decisionDTO));
    }
}