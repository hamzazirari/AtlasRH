package com.atlasrh.atlasrh_api.mapper;

import com.atlasrh.atlasrh_api.dto.EmployeeDTO;
import com.atlasrh.atlasrh_api.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerFullName", expression = "java(employee.getManager() != null ? employee.getManager().getFirstName() + \" \" + employee.getManager().getLastName() : null)")
    EmployeeDTO toDto(Employee employee);
}