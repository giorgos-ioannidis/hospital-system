package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.leave.LeaveStatus;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.notification.AppointmentEvent;
import gr.ihu.hospital.repository.AbsenceRepository;
import gr.ihu.hospital.repository.AppointmentRepository;
import gr.ihu.hospital.repository.LeaveRequestRepository;
import gr.ihu.hospital.repository.OfficeHoursRepository;
import gr.ihu.hospital.repository.RoomRepository;
import gr.ihu.hospital.repository.ScheduleRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.web.dto.OfficeHoursForm;

/**
 * UC-11 Ορισμός Ωρών Υποδοχής (FR-11, §7.5). Απορρίπτονται ώρες που συγκρούονται με βάρδια/εφημερία,
 * άδεια/απουσία, άλλες ώρες του ιατρού ή κράτηση της αίθουσας. Η διαγραφή με κλεισμένα ραντεβού θέλει
 * επιβεβαίωση, ακυρώνει τα ραντεβού και ειδοποιεί τους ασθενείς.
 */
@Service
@Transactional
public class OfficeHoursService {

    private static final int SHOWN_DAYS = 60;

    private final OfficeHoursRepository officeHours;
    private final RoomRepository rooms;
    private final AppointmentRepository appointments;
    private final ScheduleRepository schedules;
    private final LeaveRequestRepository leaves;
    private final AbsenceRepository absences;
    private final UserRepository users;
    private final ApplicationEventPublisher events;

    public OfficeHoursService(OfficeHoursRepository officeHours, RoomRepository rooms, AppointmentRepository appointments,
                              ScheduleRepository schedules, LeaveRequestRepository leaves, AbsenceRepository absences,
                              UserRepository users, ApplicationEventPublisher events) {
        this.officeHours = officeHours;
        this.rooms = rooms;
        this.appointments = appointments;
        this.schedules = schedules;
        this.leaves = leaves;
        this.absences = absences;
        this.users = users;
        this.events = events;
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public OfficeHours define(String username, OfficeHoursForm form) {
        Doctor me = users.getByUsername(username, Doctor.class);
        Room room = rooms.findById(form.getRoomId()).orElseThrow();
        if (!room.getDepartment().getId().equals(me.getDepartment().getId())) {
            throw new IllegalArgumentException("Η αίθουσα ανήκει σε άλλο τμήμα.");
        }
        OfficeHours hours = new OfficeHours(me, room, form.getDate(), form.getStartTime(), form.getEndTime(),
                                            form.getSlotMinutes());
        List<String> conflicts = conflicts(hours);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", conflicts));
        }
        return officeHours.save(hours);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional(readOnly = true)
    public List<OfficeHours> mine(String username) {
        LocalDate today = LocalDate.now();
        return officeHours.findByDoctorAndDateBetweenOrderByDateAscStartTimeAsc(
            users.getByUsername(username, Doctor.class), today, today.plusDays(SHOWN_DAYS));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional(readOnly = true)
    public List<Room> rooms(String username) {
        return rooms.findByDepartmentOrderByNameAsc(users.getByUsername(username, Doctor.class).getDepartment());
    }

    /** Πόσα ενεργά ραντεβού έχουν κλειστεί μέσα σε αυτές τις ώρες (για την επιβεβαίωση της διαγραφής). */
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional(readOnly = true)
    public int bookedCount(String username, Long id) {
        return booked(own(username, id)).size();
    }

    /** Διαγράφει τις ώρες· τα ραντεβού τους ακυρώνονται και οι ασθενείς ειδοποιούνται. Επιστρέφει πόσα ακυρώθηκαν. */
    @PreAuthorize("hasRole('DOCTOR')")
    public int remove(String username, Long id) {
        OfficeHours hours = own(username, id);
        List<Appointment> booked = booked(hours);
        for (Appointment appointment : booked) {
            appointment.cancelByHospital();
            events.publishEvent(new AppointmentEvent(appointment, AppointmentEvent.Type.CANCELLED));
        }
        officeHours.delete(hours);
        return booked.size();
    }

    private OfficeHours own(String username, Long id) {
        OfficeHours hours = officeHours.findById(id).orElseThrow();
        if (!hours.getDoctor().getUsername().equals(username)) {
            throw new AccessDeniedException("Δεν είναι δικές σας ώρες υποδοχής.");
        }
        return hours;
    }

    private List<Appointment> booked(OfficeHours hours) {
        TimeSlot interval = hours.getInterval();
        return appointments.findActiveByDoctorBetween(hours.getDoctor(), interval.getStart(), interval.getEnd(),
                                                      AppointmentStatus.CANCELLED);
    }

    private List<String> conflicts(OfficeHours hours) {
        List<String> found = new ArrayList<>();
        if (hours.getDate().isBefore(LocalDate.now())) {
            found.add("Η ημερομηνία έχει περάσει.");
        }
        if (onShift(hours)) {
            found.add("Συγκρούεται με βάρδια ή εφημερία σας.");
        }
        if (absent(hours)) {
            found.add("Είστε σε άδεια ή απουσία εκείνη την ημέρα.");
        }
        if (overlapsAny(hours, officeHours.findByDoctorAndDate(hours.getDoctor(), hours.getDate()))) {
            found.add("Επικαλύπτεται με άλλες ώρες υποδοχής σας.");
        }
        if (overlapsAny(hours, officeHours.findByRoomAndDate(hours.getRoom(), hours.getDate()))) {
            found.add("Η αίθουσα " + hours.getRoom().getName() + " είναι κρατημένη αυτές τις ώρες.");
        }
        return found;
    }

    private boolean onShift(OfficeHours hours) {
        TimeSlot interval = hours.getInterval();
        for (Assignment assignment : schedules.findAssignments(hours.getDoctor(), hours.getDate().minusDays(1),
                hours.getDate(), ScheduleStatus.ACTIVE)) {
            if (interval.overlaps(assignment.getShift().getStart(), assignment.getShift().getEnd())) {
                return true;
            }
        }
        return false;
    }

    private boolean absent(OfficeHours hours) {
        List<Unavailability> off = new ArrayList<Unavailability>(
            leaves.findOverlapping(LeaveStatus.APPROVED, hours.getDate(), hours.getDate()));
        off.addAll(absences.findOverlapping(hours.getDate(), hours.getDate()));
        TimeSlot interval = hours.getInterval();
        for (Unavailability unavailability : off) {
            if (unavailability.blocks(hours.getDoctor(), interval.getStart(), interval.getEnd())) {
                return true;
            }
        }
        return false;
    }

    private static boolean overlapsAny(OfficeHours hours, List<OfficeHours> others) {
        for (OfficeHours other : others) {
            if (other != hours && other.getInterval().overlaps(hours.getInterval())) {
                return true;
            }
        }
        return false;
    }
}
