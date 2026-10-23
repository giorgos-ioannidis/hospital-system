package gr.ihu.hospital.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.admin.Overtime;
import gr.ihu.hospital.domain.admin.OvertimeStatus;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.repository.DepartmentRepository;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.repository.OvertimeRepository;
import gr.ihu.hospital.repository.ScheduleRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.scheduling.OvertimeCalculator;

/** UC-23 Έγκριση Υπερωριών (FR-25): υπολογισμός από το ενεργό πρόγραμμα και απόφαση του Προϊσταμένου. */
@Service
@Transactional
public class AdminService {

    private final OvertimeCalculator calculator = new OvertimeCalculator();
    private final OvertimeRepository overtimes;
    private final ScheduleRepository schedules;
    private final HealthStaffRepository staff;
    private final DepartmentRepository departments;
    private final UserRepository users;

    public AdminService(OvertimeRepository overtimes, ScheduleRepository schedules, HealthStaffRepository staff,
                        DepartmentRepository departments, UserRepository users) {
        this.overtimes = overtimes;
        this.schedules = schedules;
        this.staff = staff;
        this.departments = departments;
        this.users = users;
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<OvertimeRow> overtime(Long departmentId, YearMonth month) {
        Department department = departments.findById(departmentId).orElseThrow();
        List<OvertimeRow> rows = new ArrayList<>();
        for (HealthStaff member : staff.findByDepartmentOrderByLastNameAscFirstNameAsc(department)) {
            List<Assignment> worked = assignments(member, month);
            rows.add(new OvertimeRow(member, hoursOf(worked), calculator.overtimeHours(worked),
                overtimes.findByStaffAndPeriodStart(member, month.atDay(1)).orElse(null)));
        }
        return rows;
    }

    /** Εγκρίνει ή απορρίπτει τις υπερωρίες του μήνα, όπως τις υπολογίζει τώρα το πρόγραμμα. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Overtime decide(Long staffId, YearMonth month, boolean approve, String username) {
        HealthStaff member = staff.findById(staffId).orElseThrow();
        int hours = calculator.overtimeHours(assignments(member, month));
        OvertimeStatus status = approve ? OvertimeStatus.APPROVED : OvertimeStatus.REJECTED;
        Supervisor supervisor = users.getByUsername(username, Supervisor.class);
        Overtime existing = overtimes.findByStaffAndPeriodStart(member, month.atDay(1)).orElse(null);
        if (existing != null) {
            existing.redecide(hours, status, supervisor, LocalDateTime.now());
            return existing;
        }
        return overtimes.save(new Overtime(member, month, hours, status, supervisor, LocalDateTime.now()));
    }

    private List<Assignment> assignments(HealthStaff member, YearMonth month) {
        return schedules.findAssignments(member, month.atDay(1), month.atEndOfMonth(), ScheduleStatus.ACTIVE);
    }

    private static int hoursOf(List<Assignment> assignments) {
        int hours = 0;
        for (Assignment assignment : assignments) {
            hours += assignment.getShift().getHours();
        }
        return hours;
    }

    /** Μία γραμμή του UI-21. */
    public static final class OvertimeRow {

        private final HealthStaff staff;
        private final int scheduledHours;
        private final int overtimeHours;
        private final Overtime decision;

        OvertimeRow(HealthStaff staff, int scheduledHours, int overtimeHours, Overtime decision) {
            this.staff = staff;
            this.scheduledHours = scheduledHours;
            this.overtimeHours = overtimeHours;
            this.decision = decision;
        }

        public HealthStaff getStaff() {
            return staff;
        }

        public int getScheduledHours() {
            return scheduledHours;
        }

        public int getOvertimeHours() {
            return overtimeHours;
        }

        public Overtime getDecision() {
            return decision;
        }

        /** Η απόφαση αφορά άλλες ώρες από τις σημερινές (π.χ. άλλαξε το πρόγραμμα μετά την έγκριση). */
        public boolean isOutdated() {
            return decision != null && decision.getHours() != overtimeHours;
        }
    }
}
