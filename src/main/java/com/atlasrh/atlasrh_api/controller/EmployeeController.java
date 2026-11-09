package com.atlasrh.atlasrh_api.controller;

import com.atlasrh.atlasrh_api.dto.EmployeeDTO;
import com.atlasrh.atlasrh_api.dto.EmployeeRequestDTO;
import com.atlasrh.atlasrh_api.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeDTO created = employeeService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            Pageable pageable) {
        return ResponseEntity.ok(employeeService.search(name, departmentId, pageable));
    }

    @GetMapping("/{managerId}/team")
    public ResponseEntity<List<EmployeeDTO>> getTeam(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeeService.getTeam(managerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        return ResponseEntity.ok(employeeService.update(id, requestDTO));
    }
}