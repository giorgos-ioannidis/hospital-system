package gr.ihu.hospital.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.user.HealthStaff;

/** Aggregate Schedule → Shift → Assignment. */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByStatusNotOrderByPeriodStartDescIdDesc(ScheduleStatus status);

    List<Schedule> findByDepartmentAndPeriodStartAndStatus(Department department, LocalDate periodStart,
                                                           ScheduleStatus status);

    List<Schedule> findByPeriodStartAndStatus(LocalDate periodStart, ScheduleStatus status);

    List<Schedule> findByStatusAndPeriodStartGreaterThanEqualOrderByPeriodStartAsc(ScheduleStatus status,
                                                                                   LocalDate from);

    @Query("select s from Shift s where s.id = :id")
    Optional<Shift> findShiftById(Long id);

    @Query("select a from Assignment a where a.staff = :staff and a.shift.date between :from and :to"
         + " and a.shift.schedule.status = :status order by a.shift.date")
    List<Assignment> findAssignments(HealthStaff staff, LocalDate from, LocalDate to, ScheduleStatus status);
}
