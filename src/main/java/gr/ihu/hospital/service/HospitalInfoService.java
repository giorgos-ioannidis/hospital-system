package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.MedicalService;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.organization.Ward;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.repository.DepartmentRepository;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.repository.MedicalServiceRepository;
import gr.ihu.hospital.repository.OfficeHoursRepository;
import gr.ihu.hospital.repository.RoomRepository;
import gr.ihu.hospital.repository.WardRepository;

/** UC-12 Πληροφορίες Νοσοκομείου (FR-12 κατάλογος ιατρών, FR-28 υπηρεσίες) και UC-25 Χάρτης (FR-27). */
@Service
@Transactional(readOnly = true)
public class HospitalInfoService {

    private final DepartmentRepository departments;
    private final HealthStaffRepository staff;
    private final MedicalServiceRepository services;
    private final RoomRepository rooms;
    private final WardRepository wards;
    private final OfficeHoursRepository officeHours;

    public HospitalInfoService(DepartmentRepository departments, HealthStaffRepository staff,
                               MedicalServiceRepository services, RoomRepository rooms, WardRepository wards,
                               OfficeHoursRepository officeHours) {
        this.departments = departments;
        this.staff = staff;
        this.services = services;
        this.rooms = rooms;
        this.wards = wards;
        this.officeHours = officeHours;
    }

    public List<DepartmentInfo> directory() {
        List<DepartmentInfo> info = new ArrayList<>();
        for (Department department : departments.findAllByOrderByNameAsc()) {
            info.add(new DepartmentInfo(department, staff.findDoctors(department),
                services.findByDepartmentOrderByIdAsc(department), rooms.findByDepartmentOrderByNameAsc(department),
                wards.findByDepartmentOrderByNameAsc(department)));
        }
        return info;
    }

    /** Ο χάρτης: τα τμήματα ανά όροφο (ισόγειο πρώτα). */
    public Map<Integer, List<DepartmentInfo>> floors() {
        Map<Integer, List<DepartmentInfo>> floors = new TreeMap<>();
        for (DepartmentInfo department : directory()) {
            Integer floor = department.getDepartment().getFloor() == null ? 0 : department.getDepartment().getFloor();
            floors.computeIfAbsent(floor, f -> new ArrayList<>()).add(department);
        }
        return floors;
    }

    public Room room(Long id) {
        return rooms.findById(id).orElseThrow();
    }

    /** Ποιοι ιατροί δέχονται σήμερα στην αίθουσα. */
    public List<OfficeHours> todayIn(Room room) {
        return officeHours.findByRoomAndDate(room, LocalDate.now());
    }

    /** Ένα τμήμα με ό,τι χρειάζεται ο κατάλογος και ο χάρτης. */
    public static final class DepartmentInfo {

        private final Department department;
        private final List<Doctor> doctors;
        private final List<MedicalService> services;
        private final List<Room> rooms;
        private final List<Ward> wards;

        DepartmentInfo(Department department, List<Doctor> doctors, List<MedicalService> services, List<Room> rooms,
                       List<Ward> wards) {
            this.department = department;
            this.doctors = doctors;
            this.services = services;
            this.rooms = rooms;
            this.wards = wards;
        }

        public Department getDepartment() {
            return department;
        }

        public List<Doctor> getDoctors() {
            return doctors;
        }

        public List<MedicalService> getServices() {
            return services;
        }

        public List<Room> getRooms() {
            return rooms;
        }

        public List<Ward> getWards() {
            return wards;
        }
    }
}
