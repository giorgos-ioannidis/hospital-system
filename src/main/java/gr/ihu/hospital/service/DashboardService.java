package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.leave.LeaveStatus;
import gr.ihu.hospital.domain.record.ExamStatus;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.AdmissionRepository;
import gr.ihu.hospital.repository.AppointmentRepository;
import gr.ihu.hospital.repository.ExamOrderRepository;
import gr.ihu.hospital.repository.LeaveRequestRepository;
import gr.ihu.hospital.repository.ScheduleRepository;
import gr.ihu.hospital.repository.UserRepository;

/**
 * UI-01b: ο πίνακας της αρχικής ανά ρόλο. Μόνο μετρήσεις και ό,τι αφορά τον ίδιο τον χρήστη·
 * κανένα κλινικό δεδομένο τρίτου, άρα δεν χρειάζεται καταγραφή στο AccessLog.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int AGENDA_ITEMS = 6;
    private static final int SHIFT_DAYS = 30;

    private final UserRepository users;
    private final ScheduleRepository schedules;
    private final LeaveRequestRepository leaves;
    private final AdmissionRepository admissions;
    private final AppointmentRepository appointments;
    private final ExamOrderRepository exams;
    private final BedService beds;

    public DashboardService(UserRepository users, ScheduleRepository schedules, LeaveRequestRepository leaves,
                            AdmissionRepository admissions, AppointmentRepository appointments, ExamOrderRepository exams,
                            BedService beds) {
        this.users = users;
        this.schedules = schedules;
        this.leaves = leaves;
        this.admissions = admissions;
        this.appointments = appointments;
        this.exams = exams;
        this.beds = beds;
    }

    public User me(String username) {
        return users.getByUsername(username, User.class);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public Map<String, Object> supervisor() {
        LocalDate today = LocalDate.now();
        List<Shift> uncovered = new ArrayList<>();
        int drafts = 0;
        for (Schedule schedule : schedules.findByStatusNotOrderByPeriodStartDescIdDesc(ScheduleStatus.ARCHIVED)) {
            drafts += schedule.isDraft() ? 1 : 0;
            if (!schedule.isDraft() && !schedule.getMonth().isBefore(YearMonth.from(today))) {
                schedule.getUncoveredShifts().stream().filter(s -> !s.getDate().isBefore(today)).forEach(uncovered::add);
            }
        }
        uncovered.sort((a, b) -> a.getStart().compareTo(b.getStart()));
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("pendingLeaves", leaves.findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING).size());
        model.put("uncoveredCount", uncovered.size());
        model.put("uncoveredNext", uncovered.stream().limit(AGENDA_ITEMS).collect(Collectors.toList()));
        model.put("drafts", drafts);
        model.put("admitted", admissions.findActive().size());
        model.put("wardAlerts", beds.occupancy().values().stream().flatMap(List::stream)
            .filter(BedService.Occupancy::isAlert).count());
        return model;
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public Map<String, Object> staff(String username) {
        HealthStaff me = users.getByUsername(username, HealthStaff.class);
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> next = schedules.findAssignments(me, now.toLocalDate().minusDays(1),
                now.toLocalDate().plusDays(SHIFT_DAYS), ScheduleStatus.ACTIVE).stream()
            .filter(a -> a.getShift().getEnd().isAfter(now)).limit(AGENDA_ITEMS).collect(Collectors.toList());
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("nextShifts", next);
        model.put("admittedInDepartment", admissions.findAdmittedPatients(me.getDepartment()).size());
        if (me instanceof Doctor) {
            Doctor doctor = (Doctor) me;
            List<Appointment> upcoming = appointments.findByDoctorAndStartGreaterThanEqualOrderByStartAsc(doctor,
                    now.toLocalDate().atStartOfDay()).stream()
                .filter(a -> a.isActive() && a.getEnd().isAfter(now)).collect(Collectors.toList());
            model.put("appointmentsToday", upcoming.stream()
                .filter(a -> a.getStart().toLocalDate().equals(now.toLocalDate())).count());
            model.put("upcomingAppointments", upcoming.stream().limit(AGENDA_ITEMS).collect(Collectors.toList()));
            model.put("pendingExams", exams.findByDoctorOrderByOrderedAtDesc(doctor).stream()
                .filter(e -> e.getStatus() == ExamStatus.ORDERED).count());
        }
        return model;
    }

    @PreAuthorize("hasRole('PATIENT')")
    public Map<String, Object> patient(String username) {
        Patient me = users.getByUsername(username, Patient.class);
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("upcomingAppointments", appointments.findByPatientOrderByStartDesc(me).stream()
            .filter(a -> a.isActive() && a.getStatus() != AppointmentStatus.COMPLETED && a.getStart().isAfter(now))
            .sorted((a, b) -> a.getStart().compareTo(b.getStart())).limit(3).collect(Collectors.toList()));
        model.put("resultsReady", exams.findByPatientOrderByOrderedAtDesc(me).stream()
            .filter(e -> e.getStatus() == ExamStatus.COMPLETED).count());
        model.put("pendingExams", exams.findByPatientOrderByOrderedAtDesc(me).stream()
            .filter(e -> e.getStatus() == ExamStatus.ORDERED).count());
        model.put("admitted", admissions.isAdmitted(me));
        return model;
    }
}
