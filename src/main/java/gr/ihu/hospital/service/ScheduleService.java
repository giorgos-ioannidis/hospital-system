package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.leave.LeaveStatus;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.repository.AbsenceRepository;
import gr.ihu.hospital.repository.DepartmentRepository;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.repository.LeaveRequestRepository;
import gr.ihu.hospital.repository.OfficeHoursRepository;
import gr.ihu.hospital.repository.ScheduleRepository;
import gr.ihu.hospital.repository.StaffingRequirementRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.scheduling.FairScheduleGenerator;
import gr.ihu.hospital.scheduling.GreedyScheduleGenerator;
import gr.ihu.hospital.scheduling.ScheduleGenerator;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.SchedulingContext;
import gr.ihu.hospital.scheduling.Violation;
import gr.ihu.hospital.scheduling.rules.RuleChain;
import gr.ihu.hospital.web.dto.ManualChangeForm;
import gr.ihu.hospital.web.dto.ScheduleParamsForm;

/** UC-05 Παραγωγή Προγράμματος (FR-05): παραγωγή, χειροκίνητη αλλαγή, έγκριση. UC-08 Προβολή Προγράμματος (FR-08). */
@Service
@Transactional
public class ScheduleService {

    // ponytail: μόνο οι σταθερές αργίες· οι κινητές (Πάσχα, Αγ. Πνεύματος) όταν χρειαστούν.
    private static final List<MonthDay> FIXED_HOLIDAYS = Arrays.asList(
        MonthDay.of(1, 1), MonthDay.of(1, 6), MonthDay.of(3, 25), MonthDay.of(5, 1),
        MonthDay.of(8, 15), MonthDay.of(10, 28), MonthDay.of(12, 25), MonthDay.of(12, 26));

    private final RuleChain rules = RuleChain.standard();
    private final ScheduleRepository schedules;
    private final DepartmentRepository departments;
    private final HealthStaffRepository staff;
    private final StaffingRequirementRepository requirements;
    private final LeaveRequestRepository leaves;
    private final AbsenceRepository absences;
    private final UserRepository users;
    private final OfficeHoursRepository officeHours;

    public ScheduleService(ScheduleRepository schedules, DepartmentRepository departments, HealthStaffRepository staff,
                           StaffingRequirementRepository requirements, LeaveRequestRepository leaves,
                           AbsenceRepository absences, UserRepository users, OfficeHoursRepository officeHours) {
        this.schedules = schedules;
        this.departments = departments;
        this.staff = staff;
        this.requirements = requirements;
        this.leaves = leaves;
        this.absences = absences;
        this.users = users;
        this.officeHours = officeHours;
    }

    /** Παράγει νέο DRAFT και αντικαθιστά τυχόν παλιό DRAFT του ίδιου μήνα. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Schedule generate(ScheduleParamsForm form) {
        Department department = departments.findById(form.getDepartmentId()).orElseThrow();
        YearMonth month = form.getMonth();
        ScheduleGenerator generator = form.isFair() ? new FairScheduleGenerator(rules) : new GreedyScheduleGenerator(rules);
        Schedule draft = generator.generate(context(department, month, form.getMaxWeeklyHours())).getSchedule();
        schedules.deleteAll(schedules.findByDepartmentAndPeriodStartAndStatus(department, month.atDay(1),
                                                                              ScheduleStatus.DRAFT));
        return schedules.save(draft);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public Schedule get(Long id) {
        return schedules.findById(id).orElseThrow();
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<Schedule> list() {
        return schedules.findByStatusNotOrderByPeriodStartDescIdDesc(ScheduleStatus.ARCHIVED);
    }

    /** Ακάλυπτες βάρδιες και παραβιάσεις μαλακών κανόνων, όπως τις βλέπει το RuleChain. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<Violation> review(Schedule schedule) {
        return rules.review(stateFor(schedule, false));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<HealthStaff> departmentStaff(Schedule schedule) {
        return staff.findByDepartmentOrderByLastNameAscFirstNameAsc(schedule.getDepartment());
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<Department> departmentList() {
        return departments.findAllByOrderByNameAsc();
    }

    /** DRAFT → ACTIVE· το προηγούμενο ACTIVE του ίδιου μήνα αρχειοθετείται. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public void approve(Long id, String username) {
        Schedule schedule = get(id);
        for (Schedule old : schedules.findByDepartmentAndPeriodStartAndStatus(schedule.getDepartment(),
                schedule.getPeriodStart(), ScheduleStatus.ACTIVE)) {
            old.archive();
        }
        schedule.approve(users.getByUsername(username, Supervisor.class));
    }

    /** FR-08: οι βάρδιες του εργαζομένου στα ενεργά προγράμματα του μήνα. */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Transactional(readOnly = true)
    public List<Assignment> mySchedule(String username, YearMonth month) {
        HealthStaff me = users.getByUsername(username, HealthStaff.class);
        return schedules.findAssignments(me, month.atDay(1), month.atEndOfMonth(), ScheduleStatus.ACTIVE);
    }

    /** Άδεια ή απουσία: οι βάρδιες του εργαζομένου στο διάστημα γίνονται ακάλυπτες. Επιστρέφει πόσες. */
    public int releaseAssignments(HealthStaff member, LocalDate from, LocalDate to) {
        int released = 0;
        for (ScheduleStatus status : Arrays.asList(ScheduleStatus.DRAFT, ScheduleStatus.ACTIVE)) {
            for (Assignment assignment : schedules.findAssignments(member, from.minusDays(1), to, status)) {
                if (!assignment.getShift().getEnd().toLocalDate().isBefore(from)) {
                    assignment.getShift().removeAssignment(assignment);
                    released++;
                }
            }
        }
        return released;
    }

    /** Κατάσταση για τους κανόνες· με τα άλλα τμήματα ώστε να μετρούν και οι βάρδιες τους (UC-10). */
    public ScheduleState stateFor(Schedule schedule, boolean withOtherDepartments) {
        List<Shift> shifts = new ArrayList<>(schedule.getShifts());
        if (withOtherDepartments) {
            for (Schedule other : schedules.findByPeriodStartAndStatus(schedule.getPeriodStart(), ScheduleStatus.ACTIVE)) {
                if (!other.getId().equals(schedule.getId())) {
                    shifts.addAll(other.getShifts());
                }
            }
        }
        return new ScheduleState(context(schedule.getDepartment(), schedule.getMonth(), schedule.getMaxWeeklyHours()),
                                 shifts);
    }

    private SchedulingContext context(Department department, YearMonth month, int maxWeeklyHours) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth().plusDays(1);
        List<Unavailability> off = new ArrayList<Unavailability>(leaves.findOverlapping(LeaveStatus.APPROVED, from, to));
        off.addAll(absences.findOverlapping(from, to));
        off.addAll(officeHours.findByDateBetween(from, to));
        return new SchedulingContext(department, month,
            staff.findByDepartmentOrderByLastNameAscFirstNameAsc(department),
            requirements.findByDepartmentOrderByIdAsc(department), off, holidays(month), maxWeeklyHours);
    }

    private static Set<LocalDate> holidays(YearMonth month) {
        Set<LocalDate> days = new HashSet<>();
        for (MonthDay holiday : FIXED_HOLIDAYS) {
            if (holiday.getMonth() == month.getMonth()) {
                days.add(holiday.atYear(month.getYear()));
            }
        }
        return days;
    }
}
