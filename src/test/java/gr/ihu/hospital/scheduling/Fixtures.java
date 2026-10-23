package gr.ihu.hospital.scheduling;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.ihu.hospital.domain.leave.LeaveRequest;
import gr.ihu.hospital.domain.leave.LeaveType;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.schedule.StaffingRequirement;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Nurse;
import gr.ihu.hospital.domain.user.StaffAccountData;
import gr.ihu.hospital.domain.user.Supervisor;

/** Δεδομένα για τα unit tests του αλγορίθμου: μόνο αντικείμενα, χωρίς Spring και βάση. */
final class Fixtures {

    static final YearMonth NOVEMBER = YearMonth.of(2026, 11);
    static final Department CARDIOLOGY_DEPT = new Department("Καρδιολογικό", 1);
    static final Department OTHER_DEPT = new Department("Παθολογικό", 2);
    static final Specialty NURSING = new Specialty("Νοσηλευτική");
    static final Specialty CARDIOLOGY = new Specialty("Καρδιολογία");
    static final Supervisor SUPERVISOR = new Supervisor(data("sup", CARDIOLOGY_DEPT, null));

    private Fixtures() {
    }

    static List<HealthStaff> nurses(int count) {
        return nurses(count, CARDIOLOGY_DEPT);
    }

    static List<HealthStaff> nurses(int count, Department department) {
        List<HealthStaff> staff = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            staff.add(new Nurse(data("nurse" + i + department.getName(), department, NURSING)));
        }
        return staff;
    }

    static List<HealthStaff> doctors(int count) {
        List<HealthStaff> staff = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            staff.add(new Doctor(data("doctor" + i, CARDIOLOGY_DEPT, CARDIOLOGY)));
        }
        return staff;
    }

    static StaffingRequirement need(Specialty specialty, ShiftType type, int min) {
        return new StaffingRequirement(CARDIOLOGY_DEPT, specialty, type, min);
    }

    /** Πρωί, απόγευμα, νύχτα: 1 νοσηλευτής η καθεμία. */
    static List<StaffingRequirement> nursingRoundTheClock() {
        List<StaffingRequirement> needs = new ArrayList<>();
        needs.add(need(NURSING, ShiftType.MORNING, 1));
        needs.add(need(NURSING, ShiftType.AFTERNOON, 1));
        needs.add(need(NURSING, ShiftType.NIGHT, 1));
        return needs;
    }

    static SchedulingContext context(YearMonth month, List<HealthStaff> staff, List<StaffingRequirement> needs,
                                     List<? extends Unavailability> off) {
        return new SchedulingContext(CARDIOLOGY_DEPT, month, staff, needs, off,
                                     Collections.<LocalDate>emptySet(), 48);
    }

    static LeaveRequest approvedLeave(HealthStaff member, LocalDate from, LocalDate to) {
        LeaveRequest leave = new LeaveRequest(member, LeaveType.ANNUAL, from, to, "test");
        leave.approve(SUPERVISOR);
        return leave;
    }

    static List<Assignment> assignmentsOf(ScheduleResult result, HealthStaff member) {
        List<Assignment> found = new ArrayList<>();
        for (Shift shift : result.getSchedule().getShifts()) {
            Assignment assignment = shift.findAssignment(member);
            if (assignment != null) {
                found.add(assignment);
            }
        }
        return found;
    }

    private static StaffAccountData data(String username, Department department, Specialty specialty) {
        return new StaffAccountData(username, "hash", "Όνομα", username, username + "@test", null,
                                    department, specialty);
    }
}
