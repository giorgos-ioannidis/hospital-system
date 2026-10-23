package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.leave.Absence;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.notification.ShiftAssignedEvent;
import gr.ihu.hospital.notification.ShiftsUncoveredEvent;
import gr.ihu.hospital.repository.AbsenceRepository;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.repository.ScheduleRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.scheduling.Evaluation;
import gr.ihu.hospital.scheduling.ReplacementFinder;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.rules.RuleChain;
import gr.ihu.hospital.web.dto.AbsenceForm;

/** UC-09 Δήλωση Απουσίας (FR-09) και UC-10 Εύρεση Αντικαταστάτη (FR-10). */
@Service
@Transactional
public class AbsenceService {

    private final RuleChain rules = RuleChain.standard();
    private final ReplacementFinder finder = new ReplacementFinder(rules);
    private final AbsenceRepository absences;
    private final HealthStaffRepository staff;
    private final ScheduleRepository schedules;
    private final UserRepository users;
    private final ScheduleService scheduleService;
    private final ApplicationEventPublisher events;

    public AbsenceService(AbsenceRepository absences, HealthStaffRepository staff, ScheduleRepository schedules,
                          UserRepository users, ScheduleService scheduleService, ApplicationEventPublisher events) {
        this.absences = absences;
        this.staff = staff;
        this.schedules = schedules;
        this.users = users;
        this.scheduleService = scheduleService;
        this.events = events;
    }

    /** UC-09: καταχωρεί την απουσία· οι βάρδιες γίνονται ακάλυπτες και ο Προϊστάμενος ειδοποιείται (βήμα 6 → UC-10). */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public int declare(String username, AbsenceForm form) {
        HealthStaff me = users.getByUsername(username, HealthStaff.class);
        absences.save(new Absence(me, form.getStartDate(), form.getEndDate(), form.getReason()));
        int released = scheduleService.releaseAssignments(me, form.getStartDate(), form.getEndDate());
        if (released > 0) {
            events.publishEvent(new ShiftsUncoveredEvent(me, released));
        }
        return released;
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Transactional(readOnly = true)
    public List<Absence> mine(String username) {
        return absences.findByStaffOrderByStartDateDesc(users.getByUsername(username, HealthStaff.class));
    }

    /** UI-10: οι ακάλυπτες βάρδιες των ενεργών προγραμμάτων από σήμερα, με τους προτεινόμενους αναπληρωτές. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<UncoveredShift> uncovered() {
        LocalDate today = LocalDate.now();
        List<UncoveredShift> found = new ArrayList<>();
        for (Schedule schedule : schedules.findByStatusAndPeriodStartGreaterThanEqualOrderByPeriodStartAsc(
                ScheduleStatus.ACTIVE, today.withDayOfMonth(1))) {
            ScheduleState state = scheduleService.stateFor(schedule, true);
            for (Shift shift : schedule.getUncoveredShifts()) {
                if (!shift.getDate().isBefore(today)) {
                    found.add(new UncoveredShift(shift, suggestions(shift, state)));
                }
            }
        }
        return found;
    }

    /** Ο Προϊστάμενος επιλέγει αναπληρωτή· ελέγχεται ξανά με τους σκληρούς κανόνες. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public HealthStaff assignReplacement(Long shiftId, Long staffId) {
        Shift shift = schedules.findShiftById(shiftId).orElseThrow();
        HealthStaff member = staff.findById(staffId).orElseThrow();
        ScheduleState state = scheduleService.stateFor(shift.getSchedule(), true);
        Assignment replacement = new Assignment(shift, member);
        Evaluation evaluation = rules.evaluate(replacement, state);
        if (!evaluation.isAllowed()) {
            throw new IllegalStateException("Ο αναπληρωτής δεν επιτρέπεται: " + evaluation.getViolations());
        }
        state.assign(replacement);
        events.publishEvent(new ShiftAssignedEvent(replacement));
        return member;
    }

    private List<HealthStaff> suggestions(Shift shift, ScheduleState state) {
        List<HealthStaff> others = staff.findBySpecialtyAndDepartmentNotOrderByLastNameAsc(shift.getSpecialty(),
            shift.getSchedule().getDepartment());
        return finder.find(shift, state, state.getContext().getStaff(), others);
    }

    /** Ακάλυπτη βάρδια με τους υποψήφιους αναπληρωτές (καλύτερος πρώτος). */
    public static final class UncoveredShift {

        private final Shift shift;
        private final List<HealthStaff> candidates;

        UncoveredShift(Shift shift, List<HealthStaff> candidates) {
            this.shift = shift;
            this.candidates = candidates;
        }

        public Shift getShift() {
            return shift;
        }

        public List<HealthStaff> getCandidates() {
            return candidates;
        }
    }
}
