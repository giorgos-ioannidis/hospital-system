package gr.ihu.hospital.scheduling;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.StaffingRequirement;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.scheduling.rules.RuleChain;

/**
 * Greedy με βαθμολόγηση (§7.1):
 * 1) υποψήφιοι όσοι περνούν τους HardRule, 2) κατάταξη με την ποινή των SoftRule,
 * 3) ανάθεση μέχρι την ελάχιστη στελέχωση, 4) περιορισμένο backtracking 1–2 ημερών,
 * 5) ό,τι μένει ακάλυπτο αναφέρεται στο ScheduleResult.
 */
public class GreedyScheduleGenerator implements ScheduleGenerator {

    private static final int BACKTRACK_DAYS = 2;

    private final RuleChain rules;

    public GreedyScheduleGenerator(RuleChain rules) {
        this.rules = rules;
    }

    @Override
    public ScheduleResult generate(SchedulingContext context) {
        Schedule schedule = createShifts(context);
        ScheduleState state = new ScheduleState(context, schedule.getShifts());
        for (Shift shift : fillOrder(schedule.getShifts(), context)) {
            fill(shift, state);
        }
        for (Shift shift : schedule.getUncoveredShifts()) {
            repair(shift, state);
        }
        return new ScheduleResult(schedule, rules.review(state));
    }

    /** Hook της Strategy: η σειρά με την οποία γεμίζουν οι βάρδιες. Εδώ χρονολογική. */
    protected List<Shift> fillOrder(List<Shift> shifts, SchedulingContext context) {
        return shifts;
    }

    private Schedule createShifts(SchedulingContext context) {
        Schedule schedule = new Schedule(context.getDepartment(), context.getMonth(), context.getMaxWeeklyHours());
        LocalDate last = context.getMonth().atEndOfMonth();
        for (LocalDate day = context.getMonth().atDay(1); !day.isAfter(last); day = day.plusDays(1)) {
            for (StaffingRequirement requirement : context.getRequirements()) {
                schedule.addShift(requirement.getSpecialty(), day, requirement.getShiftType(),
                                  requirement.getMinStaff());
            }
        }
        return schedule;
    }

    private void fill(Shift shift, ScheduleState state) {
        Assignment best = bestCandidate(shift, state);
        while (shift.isUnderstaffed() && best != null) {
            state.assign(best);
            best = bestCandidate(shift, state);
        }
    }

    /** Ο υποψήφιος με τη μικρότερη ποινή που περνά όλους τους σκληρούς κανόνες, ή null. */
    private Assignment bestCandidate(Shift shift, ScheduleState state) {
        Assignment best = null;
        int bestPenalty = Integer.MAX_VALUE;
        for (HealthStaff member : state.getContext().getStaff()) {
            Assignment candidate = new Assignment(shift, member);
            Evaluation evaluation = rules.evaluate(candidate, state);
            if (evaluation.isAllowed() && evaluation.getPenalty() < bestPenalty) {
                best = candidate;
                bestPenalty = evaluation.getPenalty();
            }
        }
        return best;
    }

    /** Βήμα 4: για ακάλυπτη βάρδια, μετακινεί άτομα από τις 1–2 προηγούμενες ημέρες, αν ξαναγεμίζουν. */
    private void repair(Shift target, ScheduleState state) {
        boolean moved = true;
        while (target.isUnderstaffed() && moved) {
            moved = moveOne(target, state);
        }
    }

    private boolean moveOne(Shift target, ScheduleState state) {
        for (Assignment earlier : recentAssignments(target, state)) {
            if (tryMove(earlier, target, state)) {
                return true;
            }
        }
        return false;
    }

    /** Αναθέσεις της ίδιας ειδικότητας στις 1–2 ημέρες πριν από τη βάρδια. */
    private static List<Assignment> recentAssignments(Shift target, ScheduleState state) {
        LocalDate from = target.getDate().minusDays(BACKTRACK_DAYS);
        List<Assignment> found = new ArrayList<>();
        for (Shift shift : state.getShifts()) {
            boolean inWindow = !shift.getDate().isBefore(from) && shift.getDate().isBefore(target.getDate());
            if (inWindow && shift.getSpecialty().equals(target.getSpecialty())) {
                found.addAll(shift.getAssignments());
            }
        }
        return found;
    }

    private boolean tryMove(Assignment earlier, Shift target, ScheduleState state) {
        state.unassign(earlier);
        Assignment moved = new Assignment(target, earlier.getStaff());
        if (rules.evaluate(moved, state).isAllowed()) {
            state.assign(moved);
            Assignment refill = bestCandidate(earlier.getShift(), state);
            if (refill != null) {
                state.assign(refill);
                return true;
            }
            state.unassign(moved);
        }
        state.assign(earlier);
        return false;
    }
}
