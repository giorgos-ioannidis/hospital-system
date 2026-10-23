package gr.ihu.hospital.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.HealthStaff;

/** FR-25: υπερωρίες πάνω από τις 40 ώρες ανά εβδομάδα. */
class OvertimeCalculatorTest {

    @Test
    void overtime_countsHoursAbove40PerWeek() {
        HealthStaff nurse = Fixtures.nurses(1).get(0);
        Schedule schedule = new Schedule(Fixtures.CARDIOLOGY_DEPT, Fixtures.NOVEMBER, 72);
        List<Assignment> assignments = new ArrayList<>();
        for (int day = 2; day <= 7; day++) {  // Δευ 2/11 – Σαβ 7/11: 6 × 8 = 48 ώρες
            assignments.add(new Assignment(schedule.addShift(Fixtures.NURSING, Fixtures.NOVEMBER.atDay(day), ShiftType.MORNING, 1), nurse));
        }
        for (int day = 9; day <= 11; day++) {  // επόμενη εβδομάδα: 24 ώρες, καμία υπερωρία
            assignments.add(new Assignment(schedule.addShift(Fixtures.NURSING, Fixtures.NOVEMBER.atDay(day), ShiftType.MORNING, 1), nurse));
        }

        assertThat(new OvertimeCalculator().overtimeHours(assignments)).isEqualTo(8);
    }
}
