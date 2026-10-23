package gr.ihu.hospital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.HealthStaff;
import jakarta.persistence.LockModeType;

public interface HealthStaffRepository extends JpaRepository<HealthStaff, Long> {

    List<HealthStaff> findByDepartmentOrderByLastNameAscFirstNameAsc(Department department);

    @Query("select d from Doctor d where d.department = :department and d.enabled = true order by d.lastName")
    List<Doctor> findDoctors(Department department);

    /** Κλείδωμα γραμμής (SELECT … FOR UPDATE): δύο κρατήσεις στον ίδιο ιατρό εκτελούνται η μία μετά την άλλη. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.id = :id")
    Optional<Doctor> lockDoctor(Long id);

    List<HealthStaff> findBySpecialtyAndDepartmentNotOrderByLastNameAsc(Specialty specialty, Department department);
}
