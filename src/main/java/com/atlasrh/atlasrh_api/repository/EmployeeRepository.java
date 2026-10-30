package com.atlasrh.atlasrh_api.repository;

import com.atlasrh.atlasrh_api.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByMatricule(String matricule);

    boolean existsByEmail(String email);

    Optional<Employee> findByMatricule(String matricule);

    List<Employee> findByManagerId(Long managerId);

    @Query("SELECT e FROM Employee e WHERE " +
            "(:name IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "   OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:departmentId IS NULL OR e.department.id = :departmentId)")
    Page<Employee> search(@Param("name") String name, @Param("departmentId") Long departmentId, Pageable pageable);
}