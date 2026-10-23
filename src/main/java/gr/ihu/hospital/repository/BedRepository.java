package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.organization.Bed;
import gr.ihu.hospital.domain.organization.Department;
import jakarta.persistence.LockModeType;

public interface BedRepository extends JpaRepository<Bed, Long> {

    @Query("select b from Bed b where b.ward.department = :department order by b.ward.name, b.label")
    List<Bed> findInDepartment(Department department);

    @Query("select b from Bed b where b.ward.department <> :department order by b.ward.department.name, b.ward.name, b.label")
    List<Bed> findOutsideDepartment(Department department);

    @Query("select b from Bed b order by b.ward.department.name, b.ward.name, b.label")
    List<Bed> findAllOrdered();

    /** Κλείδωμα κατά την εισαγωγή: η ίδια κλίνη δεν δίνεται σε δύο ασθενείς ταυτόχρονα. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.id = :id")
    java.util.Optional<Bed> lockById(Long id);
}
