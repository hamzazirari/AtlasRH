package com.atlasrh.atlasrh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String matricule;
    private String lastName;
    private String firstName;
    private String email;
    private String jobTitle;
    private LocalDate hireDate;
    private Long departmentId;
    private String departmentName;
    private Long managerId;
    private String managerFullName;
}