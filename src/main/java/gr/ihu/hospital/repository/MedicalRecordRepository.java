package gr.ihu.hospital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.record.MedicalRecord;
import gr.ihu.hospital.domain.user.Patient;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByPatient(Patient patient);
}
