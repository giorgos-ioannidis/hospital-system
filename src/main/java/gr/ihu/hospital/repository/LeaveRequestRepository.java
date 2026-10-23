package gr.ihu.hospital.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.leave.LeaveRequest;
import gr.ihu.hospital.domain.leave.LeaveStatus;
import gr.ihu.hospital.domain.user.HealthStaff;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("select l from LeaveRequest l where l.status = :status and l.startDate <= :to and l.endDate >= :from")
    List<LeaveRequest> findOverlapping(LeaveStatus status, LocalDate from, LocalDate to);

    @Query("select count(l) > 0 from LeaveRequest l where l.staff = :staff and l.status <> :ignored"
         + " and l.startDate <= :to and l.endDate >= :from")
    boolean existsOverlapping(HealthStaff staff, LocalDate from, LocalDate to, LeaveStatus ignored);

    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveStatus status);

    List<LeaveRequest> findByStaffOrderByStartDateDesc(HealthStaff staff);
}
