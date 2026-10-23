package gr.ihu.hospital.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.leave.Absence;
import gr.ihu.hospital.domain.user.HealthStaff;

public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    @Query("select a from Absence a where a.startDate <= :to and a.endDate >= :from")
    List<Absence> findOverlapping(LocalDate from, LocalDate to);

    List<Absence> findByStaffOrderByStartDateDesc(HealthStaff staff);
}
