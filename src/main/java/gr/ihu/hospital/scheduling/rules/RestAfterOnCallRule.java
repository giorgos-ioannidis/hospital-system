package gr.ihu.hospital.scheduling.rules;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.scheduling.ScheduleState;

/** Ρεπό την επόμενη μέρα μετά από εφημερία, άρα ποτέ 2 συνεχόμενες εφημερίες. Ελέγχει και τις δύο κατευθύνσεις. */
public class RestAfterOnCallRule extends HardRule {

    public RestAfterOnCallRule() {
        super("Ρεπό μετά από εφημερία");
    }

    @Override
    protected boolean allows(Assignment candidate, ScheduleState state) {
        HealthStaff member = candidate.getStaff();
        Shift shift = candidate.getShift();
        if (state.hasShift(member, shift.getDate().minusDays(1), ShiftType.ON_CALL)) {
            return false;
        }
        return shift.getType() != ShiftType.ON_CALL || !state.worksOn(member, shift.getDate().plusDays(1));
    }
}
