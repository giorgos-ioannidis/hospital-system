package gr.ihu.hospital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.organization.Equipment;
import jakarta.persistence.LockModeType;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /** Κλείδωμα γραμμής (SELECT … FOR UPDATE) κατά την κράτηση: δύο κρατήσεις του ίδιου εξοπλισμού σειριοποιούνται. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipment e where e.id = :id")
    Optional<Equipment> lockById(Long id);
}
