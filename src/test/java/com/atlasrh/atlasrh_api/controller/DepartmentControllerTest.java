package com.atlasrh.atlasrh_api.controller;

import com.atlasrh.atlasrh_api.dto.DepartmentDTO;
import com.atlasrh.atlasrh_api.dto.DepartmentRequestDTO;
import com.atlasrh.atlasrh_api.exception.ConflictException;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import com.atlasrh.atlasrh_api.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)

class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private DepartmentDTO departmentDTO;
    private DepartmentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        departmentDTO = DepartmentDTO.builder()
                .id(1L)
                .name("IT")
                .description("Service informatique")
                .build();

        requestDTO = DepartmentRequestDTO.builder()
                .name("IT")
                .description("Service informatique")
                .build();
    }

    // Teste la création d'un département valide -> doit retourner 201 avec le DTO créé
    @Test
    void shouldReturn201WhenCreatingValidDepartment() throws Exception {
        when(departmentService.create(any(DepartmentRequestDTO.class))).thenReturn(departmentDTO);

        mockMvc.perform(post("/api/departments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("IT"));
    }

    // Teste la création d'un département avec un nom vide -> doit retourner 400 (validation Bean Validation)
    @Test
    void shouldReturn400WhenCreatingDepartmentWithBlankName() throws Exception {
        requestDTO.setName("");

        mockMvc.perform(post("/api/departments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    // Teste la création d'un département avec un nom déjà existant -> doit retourner 409
    @Test
    void shouldReturn409WhenDepartmentNameAlreadyExists() throws Exception {
        when(departmentService.create(any(DepartmentRequestDTO.class)))
                .thenThrow(new ConflictException("Un département avec ce nom existe déjà : IT"));

        mockMvc.perform(post("/api/departments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    // Teste la récupération d'un département existant par son id -> doit retourner 200 avec le bon JSON
    @Test
    void shouldReturn200WhenGettingExistingDepartment() throws Exception {
        when(departmentService.getById(1L)).thenReturn(departmentDTO);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"));
    }

    // Teste la récupération d'un département inexistant -> doit retourner 404
    @Test
    void shouldReturn404WhenDepartmentDoesNotExist() throws Exception {
        when(departmentService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Département introuvable avec l'id : 99"));

        mockMvc.perform(get("/api/departments/99"))
                .andExpect(status().isNotFound());
    }

    // Teste la suppression d'un département -> doit retourner 204 No Content
    @Test
    void shouldReturn204WhenDeletingDepartment() throws Exception {
        doNothing().when(departmentService).delete(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isNoContent());
    }
}
