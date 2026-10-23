package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.schedule.StaffingRequirement;

public interface StaffingRequirementRepository extends JpaRepository<StaffingRequirement, Long> {

    List<StaffingRequirement> findByDepartmentOrderByIdAsc(Department department);
}
