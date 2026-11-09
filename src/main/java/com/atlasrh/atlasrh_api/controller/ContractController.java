package com.atlasrh.atlasrh_api.controller;

import com.atlasrh.atlasrh_api.dto.ContractDTO;
import com.atlasrh.atlasrh_api.dto.ContractRequestDTO;
import com.atlasrh.atlasrh_api.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractDTO> create(@Valid @RequestBody ContractRequestDTO requestDTO) {
        ContractDTO created = contractService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/terminate")
    public ResponseEntity<ContractDTO> terminate(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.terminate(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ContractDTO>> getHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(contractService.getHistory(employeeId));
    }
}