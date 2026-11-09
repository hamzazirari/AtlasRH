package com.atlasrh.atlasrh_api.repository;

import com.atlasrh.atlasrh_api.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    Optional<Contract> findByEmployeeIdAndEndDateIsNullOrEndDateGreaterThanEqual(Long employeeId, LocalDate today);
}