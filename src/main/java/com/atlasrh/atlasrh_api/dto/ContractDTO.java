package com.atlasrh.atlasrh_api.dto;

import com.atlasrh.atlasrh_api.enums.ContractType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractDTO {
    private Long id;
    private ContractType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal grossSalary;
    private Long employeeId;
    private String employeeFullName;
}