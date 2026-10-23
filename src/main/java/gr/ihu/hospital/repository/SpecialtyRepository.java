package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.organization.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    List<Specialty> findAllByOrderByNameAsc();
}
