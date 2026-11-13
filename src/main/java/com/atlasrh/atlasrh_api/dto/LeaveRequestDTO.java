package com.atlasrh.atlasrh_api.dto;

import com.atlasrh.atlasrh_api.enums.LeaveStatus;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequestDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer workingDays;
    private String reason;
    private LeaveStatus status;
    private String comment;
    private Long employeeId;
    private String employeeFullName;
    private Long leaveTypeId;
    private String leaveTypeName;
}