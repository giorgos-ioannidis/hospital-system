package gr.ihu.hospital.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;
import gr.ihu.hospital.scheduling.rules.RuleChain;
import gr.ihu.hospital.web.dto.ManualChangeForm;

/** UC-05: χειροκίνητη αλλαγή ανάθεσης στο πρόγραμμα, με τους ίδιους κανόνες (RuleChain) με τον αλγόριθμο. */
@Service
@Transactional
public class ScheduleChangeService {

    private final RuleChain rules = RuleChain.standard();
    private final ScheduleService schedules;
    private final HealthStaffRepository staff;

    public ScheduleChangeService(ScheduleService schedules, HealthStaffRepository staff) {
        this.schedules = schedules;
        this.staff = staff;
    }

    /** Χειροκίνητη αλλαγή: η αλλαγή περνά από το ίδιο RuleChain· με προειδοποιήσεις εφαρμόζεται μόνο με επιβεβαίωση. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ChangeResult change(Long scheduleId, ManualChangeForm form) {
        Schedule schedule = schedules.get(scheduleId);
        Shift shift = findShift(schedule, form);
        HealthStaff member = staff.findById(form.getStaffId()).orElseThrow();
        ScheduleState state = schedules.stateFor(schedule, true);
        return form.isAdd() ? add(shift, member, state, form.isConfirm()) : remove(shift, member, state, form.isConfirm());
    }

    private ChangeResult add(Shift shift, HealthStaff member, ScheduleState state, boolean confirm) {
        if (shift.isAssigned(member)) {
            throw new IllegalArgumentException(member.getFullName() + " είναι ήδη στη βάρδια.");
        }
        Assignment candidate = new Assignment(shift, member);
        List<Violation> warnings = rules.evaluate(candidate, state).getViolations();
        boolean apply = warnings.isEmpty() || confirm;
        if (apply) {
            state.assign(candidate);
        }
        return new ChangeResult(apply, warnings);
    }

    private ChangeResult remove(Shift shift, HealthStaff member, ScheduleState state, boolean confirm) {
        Assignment assignment = shift.findAssignment(member);
        if (assignment == null) {
            throw new IllegalArgumentException(member.getFullName() + " δεν είναι σε αυτή τη βάρδια.");
        }
        List<Violation> warnings = new ArrayList<>();
        if (shift.getAssignments().size() - 1 < shift.getMinStaff()) {
            warnings.add(new Violation("Ελάχιστη στελέχωση", true, shift.getLabel() + ": θα μείνει ακάλυπτη"));
        }
        boolean apply = warnings.isEmpty() || confirm;
        if (apply) {
            state.unassign(assignment);
        }
        return new ChangeResult(apply, warnings);
    }

    private static Shift findShift(Schedule schedule, ManualChangeForm form) {
        String[] slot = form.getSlot().split(":");
        Long specialtyId = Long.valueOf(slot[0]);
        ShiftType type = ShiftType.valueOf(slot[1]);
        for (Shift shift : schedule.getShifts()) {
            if (shift.getDate().equals(form.getDate()) && shift.getType() == type
                    && shift.getSpecialty().getId().equals(specialtyId)) {
                return shift;
            }
        }
        throw new NoSuchElementException("Δεν υπάρχει τέτοια βάρδια στο πρόγραμμα.");
    }
}
