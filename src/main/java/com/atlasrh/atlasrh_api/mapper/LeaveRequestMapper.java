package com.atlasrh.atlasrh_api.mapper;

import com.atlasrh.atlasrh_api.dto.LeaveRequestDTO;
import com.atlasrh.atlasrh_api.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeFullName", expression = "java(leaveRequest.getEmployee().getFirstName() + \" \" + leaveRequest.getEmployee().getLastName())")
    @Mapping(target = "leaveTypeId", source = "leaveType.id")
    @Mapping(target = "leaveTypeName", source = "leaveType.name")
    @Mapping(target = "workingDays", ignore = true)
    LeaveRequestDTO toDto(LeaveRequest leaveRequest);
}