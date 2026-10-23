package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Ward;

public interface WardRepository extends JpaRepository<Ward, Long> {

    List<Ward> findByDepartmentOrderByNameAsc(Department department);
}
