package gr.ihu.hospital.scheduling;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.HealthStaff;

/**
 * Η τρέχουσα κατάσταση του προγράμματος κατά την παραγωγή ή τον έλεγχο μιας αλλαγής.
 * Κρατά ευρετήριο αναθέσεων ανά εργαζόμενο, ώστε οι κανόνες να ελέγχουν γρήγορα.
 */
public final class ScheduleState {

    private static final OvertimeCalculator OVERTIME = new OvertimeCalculator();

    private final SchedulingContext context;
    private final List<Shift> shifts;
    private final Map<HealthStaff, List<Assignment>> byStaff = new HashMap<>();

    public ScheduleState(SchedulingContext context, List<Shift> shifts) {
        this.context = context;
        this.shifts = new ArrayList<>(shifts);
        for (Shift shift : shifts) {
            for (Assignment assignment : shift.getAssignments()) {
                index(assignment);
            }
        }
    }

    public void assign(Assignment assignment) {
        assignment.getShift().addAssignment(assignment);
        index(assignment);
    }

    public void unassign(Assignment assignment) {
        assignment.getShift().removeAssignment(assignment);
        assignmentsOf(assignment.getStaff()).remove(assignment);
    }

    private void index(Assignment assignment) {
        byStaff.computeIfAbsent(assignment.getStaff(), s -> new ArrayList<>()).add(assignment);
    }

    private List<Assignment> assignmentsOf(HealthStaff member) {
        return byStaff.computeIfAbsent(member, s -> new ArrayList<>());
    }

    public List<Assignment> getAssignments(HealthStaff member) {
        return Collections.unmodifiableList(assignmentsOf(member));
    }

    public boolean worksOn(HealthStaff member, LocalDate date) {
        for (Assignment assignment : assignmentsOf(member)) {
            if (assignment.getShift().getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasShift(HealthStaff member, LocalDate date, ShiftType type) {
        for (Assignment assignment : assignmentsOf(member)) {
            Shift shift = assignment.getShift();
            if (shift.getDate().equals(date) && shift.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /** Ώρες του εργαζομένου στην εβδομάδα (Δευτέρα–Κυριακή) που περιέχει την ημερομηνία. */
    public int hoursInWeekOf(HealthStaff member, LocalDate date) {
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate nextMonday = monday.plusWeeks(1);
        int hours = 0;
        for (Assignment assignment : assignmentsOf(member)) {
            LocalDate day = assignment.getShift().getDate();
            if (!day.isBefore(monday) && day.isBefore(nextMonday)) {
                hours += assignment.getShift().getHours();
            }
        }
        return hours;
    }

    public int totalHours(HealthStaff member) {
        int hours = 0;
        for (Assignment assignment : assignmentsOf(member)) {
            hours += assignment.getShift().getHours();
        }
        return hours;
    }

    /** FR-10/FR-25: υπερωρίες του μήνα (ώρες πάνω από τις 40 ανά εβδομάδα). */
    public int overtimeHours(HealthStaff member) {
        return OVERTIME.overtimeHours(assignmentsOf(member));
    }

    public int weekendOrHolidayShifts(HealthStaff member) {
        int count = 0;
        for (Assignment assignment : assignmentsOf(member)) {
            if (context.isWeekendOrHoliday(assignment.getShift().getDate())) {
                count++;
            }
        }
        return count;
    }

    public SchedulingContext getContext() {
        return context;
    }

    public List<Shift> getShifts() {
        return Collections.unmodifiableList(shifts);
    }
}
