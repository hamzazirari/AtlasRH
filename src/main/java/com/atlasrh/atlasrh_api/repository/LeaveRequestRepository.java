package com.atlasrh.atlasrh_api.repository;

import com.atlasrh.atlasrh_api.entity.LeaveRequest;
import com.atlasrh.atlasrh_api.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeManagerIdAndStatus(Long managerId, LeaveStatus status);
}