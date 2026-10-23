package gr.ihu.hospital.scheduling;

import static gr.ihu.hospital.scheduling.Fixtures.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.leave.LeaveRequest;
import gr.ihu.hospital.domain.leave.LeaveType;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.HealthStaff;

/** FR-07: προειδοποίηση όταν η άδεια ρίχνει το τμήμα κάτω από την ελάχιστη στελέχωση. */
class StaffingCheckerTest {

    private final StaffingChecker checker = new StaffingChecker();

    @Test
    void leave_warnsForEveryDayTheDepartmentFallsBelowMinimum() {
        List<HealthStaff> nurses = Fixtures.nurses(3);
        LeaveRequest leave = new LeaveRequest(nurses.get(0), LeaveType.ANNUAL, NOVEMBER.atDay(5), NOVEMBER.atDay(6), null);

        List<StaffingGap> gaps = checker.check(leave, nurses, Fixtures.nursingRoundTheClock(),
                                               Collections.<Unavailability>emptyList());

        assertThat(gaps).extracting(StaffingGap::getType).containsExactly(ShiftType.NIGHT, ShiftType.NIGHT);
        assertThat(gaps).extracting(StaffingGap::getDate).containsExactly(NOVEMBER.atDay(5), NOVEMBER.atDay(6));
    }

    @Test
    void leave_noWarningWhenColleaguesSuffice() {
        List<HealthStaff> nurses = Fixtures.nurses(4);
        LeaveRequest leave = new LeaveRequest(nurses.get(0), LeaveType.ANNUAL, NOVEMBER.atDay(5), NOVEMBER.atDay(6), null);

        assertThat(checker.check(leave, nurses, Fixtures.nursingRoundTheClock(),
                                 Collections.<Unavailability>emptyList())).isEmpty();
    }
}
