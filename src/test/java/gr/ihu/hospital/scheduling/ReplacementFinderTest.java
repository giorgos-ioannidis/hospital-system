package gr.ihu.hospital.scheduling;

import static gr.ihu.hospital.scheduling.Fixtures.CARDIOLOGY_DEPT;
import static gr.ihu.hospital.scheduling.Fixtures.NOVEMBER;
import static gr.ihu.hospital.scheduling.Fixtures.NURSING;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.scheduling.rules.RuleChain;

/** FR-10 / UC-10: πρόταση αναπληρωτών. */
class ReplacementFinderTest {

    private final ReplacementFinder finder = new ReplacementFinder(RuleChain.standard());

    @Test
    void replacementFinder_ranksByFewestHours() {
        List<HealthStaff> nurses = Fixtures.nurses(4);
        HealthStaff busy = nurses.get(0);
        HealthStaff someHours = nurses.get(1);
        HealthStaff free = nurses.get(2);
        HealthStaff onLeave = nurses.get(3);
        Schedule schedule = new Schedule(CARDIOLOGY_DEPT, NOVEMBER, 48);
        Shift uncovered = schedule.addShift(NURSING, NOVEMBER.atDay(20), ShiftType.MORNING, 1);
        List<Unavailability> off = new ArrayList<>();
        off.add(Fixtures.approvedLeave(onLeave, NOVEMBER.atDay(19), NOVEMBER.atDay(21)));
        ScheduleState state = new ScheduleState(Fixtures.context(NOVEMBER, nurses, Collections.emptyList(), off),
                                                schedule.getShifts());
        work(state, schedule, busy, 3, 4, 5);
        work(state, schedule, someHours, 10);

        List<HealthStaff> ranked = finder.find(uncovered, state, nurses, Collections.<HealthStaff>emptyList());

        assertThat(ranked).containsExactly(free, someHours, busy);
    }

    @Test
    void replacementFinder_looksInOtherDepartmentsWhenNoneAvailable() {
        List<HealthStaff> own = Fixtures.nurses(1);
        List<HealthStaff> others = Fixtures.nurses(2, Fixtures.OTHER_DEPT);
        Schedule schedule = new Schedule(CARDIOLOGY_DEPT, NOVEMBER, 48);
        Shift uncovered = schedule.addShift(NURSING, NOVEMBER.atDay(20), ShiftType.NIGHT, 1);
        Shift morning = schedule.addShift(NURSING, NOVEMBER.atDay(20), ShiftType.MORNING, 1);
        ScheduleState state = new ScheduleState(
            Fixtures.context(NOVEMBER, own, Collections.emptyList(), Collections.<Unavailability>emptyList()),
            schedule.getShifts());
        state.assign(new Assignment(morning, own.get(0)));

        assertThat(finder.find(uncovered, state, own, others)).containsExactlyElementsOf(others);
    }

    private static void work(ScheduleState state, Schedule schedule, HealthStaff member, Integer... days) {
        for (int day : Arrays.asList(days)) {
            Shift shift = schedule.addShift(NURSING, NOVEMBER.atDay(day), ShiftType.MORNING, 1);
            state.assign(new Assignment(shift, member));
        }
    }
}
