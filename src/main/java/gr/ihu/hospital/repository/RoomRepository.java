package gr.ihu.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByDepartmentOrderByNameAsc(Department department);
}
