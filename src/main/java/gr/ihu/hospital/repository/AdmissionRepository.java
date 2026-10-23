package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.organization.Admission;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.user.Patient;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    @Query("select a from Admission a where a.dischargedAt is null order by a.bed.ward.department.name, a.bed.ward.name")
    List<Admission> findActive();

    @Query("select count(a) > 0 from Admission a where a.patient = :patient and a.dischargedAt is null")
    boolean isAdmitted(Patient patient);

    @Query("select count(a) > 0 from Admission a where a.patient = :patient and a.dischargedAt is null"
         + " and a.bed.ward.department = :department")
    boolean isAdmittedIn(Patient patient, Department department);

    @Query("select a.patient from Admission a where a.dischargedAt is null and a.bed.ward.department = :department"
         + " order by a.patient.lastName")
    List<Patient> findAdmittedPatients(Department department);

    List<Admission> findByPatientOrderByAdmittedAtDesc(Patient patient);
}
