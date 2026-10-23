package gr.ihu.hospital.scheduling;

import static gr.ihu.hospital.scheduling.Fixtures.CARDIOLOGY;
import static gr.ihu.hospital.scheduling.Fixtures.NOVEMBER;
import static gr.ihu.hospital.scheduling.Fixtures.NURSING;
import static gr.ihu.hospital.scheduling.Fixtures.assignmentsOf;
import static gr.ihu.hospital.scheduling.Fixtures.context;
import static gr.ihu.hospital.scheduling.Fixtures.need;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import gr.ihu.hospital.domain.leave.Absence;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.schedule.StaffingRequirement;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.scheduling.rules.RuleChain;

/** TC-1, TC-2, TC-3 (FR-05) για κάθε Strategy, συν απόδοση (NFR-01) και δίκαιη κατανομή Σ/Κ. */
class ScheduleGeneratorTest {

    static Stream<ScheduleGenerator> generators() {
        return Stream.of(new GreedyScheduleGenerator(RuleChain.standard()),
                         new FairScheduleGenerator(RuleChain.standard()));
    }

    /** TC-1: μετά από εφημερία ακολουθεί ρεπό, άρα ποτέ δύο συνεχόμενες εφημερίες. */
    @ParameterizedTest
    @MethodSource("generators")
    void generator_neverAssignsTwoConsecutiveOnCalls(ScheduleGenerator generator) {
        List<HealthStaff> doctors = Fixtures.doctors(3);
        List<StaffingRequirement> needs = Arrays.asList(need(CARDIOLOGY, ShiftType.MORNING, 1),
                                                        need(CARDIOLOGY, ShiftType.ON_CALL, 1));

        ScheduleResult result = generator.generate(context(NOVEMBER, doctors, needs, Collections.<Unavailability>emptyList()));

        int onCalls = 0;
        for (HealthStaff doctor : doctors) {
            List<Assignment> mine = assignmentsOf(result, doctor);
            for (Assignment onCall : mine) {
                if (onCall.getShift().getType() == ShiftType.ON_CALL) {
                    onCalls++;
                    LocalDate nextDay = onCall.getShift().getDate().plusDays(1);
                    assertThat(mine).noneMatch(a -> a.getShift().getDate().equals(nextDay));
                }
            }
        }
        assertThat(onCalls).as("ανατέθηκαν εφημερίες").isPositive();
    }

    /** TC-2: κανείς δεν μπαίνει σε βάρδια ενώ έχει εγκεκριμένη άδεια ή δηλωμένη απουσία. */
    @ParameterizedTest
    @MethodSource("generators")
    void generator_neverSchedulesStaffOnApprovedLeave(ScheduleGenerator generator) {
        List<HealthStaff> nurses = Fixtures.nurses(5);
        HealthStaff onLeave = nurses.get(0);
        HealthStaff sick = nurses.get(1);
        LocalDate leaveFrom = NOVEMBER.atDay(10);
        LocalDate leaveTo = NOVEMBER.atDay(20);
        List<Unavailability> off = new ArrayList<>();
        off.add(Fixtures.approvedLeave(onLeave, leaveFrom, leaveTo));
        off.add(new Absence(sick, NOVEMBER.atDay(25), NOVEMBER.atDay(27), "γρίπη"));

        ScheduleResult result = generator.generate(context(NOVEMBER, nurses, Fixtures.nursingRoundTheClock(), off));

        assertThat(assignmentsOf(result, onLeave)).as("δουλεύει εκτός άδειας").isNotEmpty();
        assertOutside(assignmentsOf(result, onLeave), leaveFrom, leaveTo);
        assertOutside(assignmentsOf(result, sick), NOVEMBER.atDay(25), NOVEMBER.atDay(27));
    }

    /** Καμία βάρδια δεν ξεκινά ή τελειώνει μέσα στο διάστημα [from, to]. */
    private static void assertOutside(List<Assignment> assignments, LocalDate from, LocalDate to) {
        for (Assignment a : assignments) {
            boolean before = a.getShift().getEnd().toLocalDate().isBefore(from);
            boolean after = a.getShift().getDate().isAfter(to);
            assertThat(before || after).as("βάρδια μέσα στο διάστημα: " + a.getShift().getLabel()).isTrue();
        }
    }

    /** TC-3: κάθε βάρδια έχει την ελάχιστη στελέχωση ή εμφανίζεται στις ακάλυπτες. */
    @ParameterizedTest
    @MethodSource("generators")
    void generator_everyShiftMeetsMinimumStaffingOrIsReportedUncovered(ScheduleGenerator generator) {
        ScheduleResult tooFew = generator.generate(
            context(NOVEMBER, Fixtures.nurses(2), Fixtures.nursingRoundTheClock(), Collections.<Unavailability>emptyList()));
        ScheduleResult enough = generator.generate(
            context(NOVEMBER, Fixtures.nurses(8), Fixtures.nursingRoundTheClock(), Collections.<Unavailability>emptyList()));

        assertCoveredOrReported(tooFew);
        assertThat(tooFew.getUncovered()).as("2 νοσηλευτές δεν φτάνουν για 3 βάρδιες/μέρα").isNotEmpty();
        assertCoveredOrReported(enough);
        assertThat(enough.getUncovered()).as("8 νοσηλευτές καλύπτουν όλο τον μήνα").isEmpty();
    }

    private static void assertCoveredOrReported(ScheduleResult result) {
        assertThat(result.getSchedule().getShifts()).hasSize(NOVEMBER.lengthOfMonth() * 3);
        for (Shift shift : result.getSchedule().getShifts()) {
            boolean covered = shift.getAssignments().size() >= shift.getMinStaff();
            assertThat(covered || result.getUncovered().contains(shift)).as(shift.getLabel()).isTrue();
        }
        assertThat(result.getUncovered()).allMatch(Shift::isUnderstaffed);
    }

    /** NFR-01: μηνιαίο πρόγραμμα ενός τμήματος (30 μέλη, 31 ημέρες) σε λιγότερο από 10 s. */
    @Test
    void generator_completesUnder10Seconds() {
        List<HealthStaff> staff = new ArrayList<>(Fixtures.nurses(20));
        staff.addAll(Fixtures.doctors(10));
        List<StaffingRequirement> needs = new ArrayList<>(Fixtures.nursingRoundTheClock());
        needs.add(need(NURSING, ShiftType.MORNING, 2));
        needs.add(need(CARDIOLOGY, ShiftType.MORNING, 2));
        needs.add(need(CARDIOLOGY, ShiftType.ON_CALL, 1));
        SchedulingContext context = context(YearMonth.of(2026, 12), staff, needs, Collections.<Unavailability>emptyList());

        ScheduleResult result = assertTimeoutPreemptively(Duration.ofSeconds(10),
            () -> new FairScheduleGenerator(RuleChain.standard()).generate(context));

        assertThat(result.getSchedule().getShifts()).hasSize(31 * needs.size());
    }

    /** Μαλακός κανόνας: η διαφορά βαρδιών Σ/Κ ανάμεσα στους νοσηλευτές είναι το πολύ 1. */
    @Test
    void weekendDifferenceAtMostOne() {
        List<HealthStaff> nurses = Fixtures.nurses(6);
        SchedulingContext context = context(NOVEMBER, nurses, Fixtures.nursingRoundTheClock(),
                                            Collections.<Unavailability>emptyList());

        ScheduleResult result = new FairScheduleGenerator(RuleChain.standard()).generate(context);

        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (HealthStaff nurse : nurses) {
            int weekends = (int) assignmentsOf(result, nurse).stream().filter(a -> a.getShift().isWeekend()).count();
            max = Math.max(max, weekends);
            min = Math.min(min, weekends);
        }
        assertThat(max - min).isLessThanOrEqualTo(1);
        assertThat(result.getUncovered()).isEmpty();
    }
}
