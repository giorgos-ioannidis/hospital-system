package gr.ihu.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Patient;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ponytail: φέρνει όλα τα ενεργά ραντεβού του παραθύρου· αρκεί για ένα νοσοκομείο, φίλτρο ανά πόρο αν μεγαλώσει.
    @Query("select a from Appointment a where a.status <> :cancelled and a.start < :to and a.end > :from")
    List<Appointment> findActiveBetween(LocalDateTime from, LocalDateTime to, AppointmentStatus cancelled);

    @Query("select a from Appointment a where a.doctor = :doctor and a.status <> :cancelled"
         + " and a.start < :to and a.end > :from order by a.start")
    List<Appointment> findActiveByDoctorBetween(Doctor doctor, LocalDateTime from, LocalDateTime to,
                                                AppointmentStatus cancelled);

    @Query("select count(a) > 0 from Appointment a where a.doctor = :doctor and a.patient = :patient"
         + " and a.status <> :cancelled")
    boolean existsActive(Doctor doctor, Patient patient, AppointmentStatus cancelled);

    @Query("select distinct a.patient from Appointment a where a.doctor = :doctor and a.status <> :cancelled")
    List<Patient> findPatientsOf(Doctor doctor, AppointmentStatus cancelled);

    List<Appointment> findByPatientOrderByStartDesc(Patient patient);

    List<Appointment> findByDoctorAndStartGreaterThanEqualOrderByStartAsc(Doctor doctor, LocalDateTime from);
}
