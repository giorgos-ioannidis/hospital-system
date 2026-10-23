package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.record.AccessLog;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    List<AccessLog> findTop50ByPatientOrderByOccurredAtDescIdDesc(Patient patient);

    List<AccessLog> findByUserAndPatient(User user, Patient patient);
}
