package gr.ihu.hospital.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.user.Doctor;

public interface OfficeHoursRepository extends JpaRepository<OfficeHours, Long> {

    List<OfficeHours> findByDoctorAndDateBetweenOrderByDateAscStartTimeAsc(Doctor doctor, LocalDate from, LocalDate to);

    List<OfficeHours> findByDateBetween(LocalDate from, LocalDate to);

    List<OfficeHours> findByRoomAndDate(Room room, LocalDate date);

    List<OfficeHours> findByDoctorAndDate(Doctor doctor, LocalDate date);
}
