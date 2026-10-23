package gr.ihu.hospital.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.Shift;

/** Έξοδος του αλγορίθμου: το πρόγραμμα (με τις αναθέσεις), οι ακάλυπτες βάρδιες, οι παραβιάσεις μαλακών κανόνων. */
public final class ScheduleResult {

    private final Schedule schedule;
    private final List<Violation> softViolations = new ArrayList<>();

    public ScheduleResult(Schedule schedule, List<Violation> violations) {
        this.schedule = schedule;
        for (Violation violation : violations) {
            if (!violation.isHard()) {
                softViolations.add(violation);
            }
        }
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public List<Shift> getUncovered() {
        return schedule.getUncoveredShifts();
    }

    public List<Violation> getSoftViolations() {
        return Collections.unmodifiableList(softViolations);
    }
}
