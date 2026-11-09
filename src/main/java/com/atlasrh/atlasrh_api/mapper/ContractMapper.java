package com.atlasrh.atlasrh_api.mapper;

import com.atlasrh.atlasrh_api.dto.ContractDTO;
import com.atlasrh.atlasrh_api.entity.Contract;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeFullName", expression = "java(contract.getEmployee().getFirstName() + \" \" + contract.getEmployee().getLastName())")
    ContractDTO toDto(Contract contract);
}