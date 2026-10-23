package gr.ihu.hospital.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.admin.Overtime;
import gr.ihu.hospital.domain.user.HealthStaff;

public interface OvertimeRepository extends JpaRepository<Overtime, Long> {

    Optional<Overtime> findByStaffAndPeriodStart(HealthStaff staff, LocalDate periodStart);
}
