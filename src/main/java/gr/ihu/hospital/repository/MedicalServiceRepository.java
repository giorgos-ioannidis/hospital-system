package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.MedicalService;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {

    List<MedicalService> findByDepartmentOrderByIdAsc(Department department);
}
