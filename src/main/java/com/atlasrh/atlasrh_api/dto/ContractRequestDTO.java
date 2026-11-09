package com.atlasrh.atlasrh_api.dto;

import com.atlasrh.atlasrh_api.enums.ContractType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class ContractRequestDTO {

    @NotNull(message = "Le type de contrat est obligatoire")
    private ContractType type;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Le salaire brut est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire doit être positif")
    private BigDecimal grossSalary;

    @NotNull(message = "L'employé est obligatoire")
    private Long employeeId;
}