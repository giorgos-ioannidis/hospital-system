package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.record.ExamOrder;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Patient;

public interface ExamOrderRepository extends JpaRepository<ExamOrder, Long> {

    List<ExamOrder> findByPatientOrderByOrderedAtDesc(Patient patient);

    List<ExamOrder> findByDoctorOrderByOrderedAtDesc(Doctor doctor);

    @Query("select count(e) > 0 from ExamOrder e where e.doctor = :doctor and e.patient = :patient")
    boolean existsByDoctorAndPatient(Doctor doctor, Patient patient);
}
