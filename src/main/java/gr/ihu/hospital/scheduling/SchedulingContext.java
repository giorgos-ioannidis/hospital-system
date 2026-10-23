package gr.ihu.hospital.scheduling;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.schedule.StaffingRequirement;
import gr.ihu.hospital.domain.user.HealthStaff;

/** Είσοδος του αλγορίθμου (FR-05): τμήμα, μήνας, προσωπικό, στελέχωση, άδειες/απουσίες, ώρες υποδοχής, αργίες, όριο ωρών. */
public final class SchedulingContext {

    private final Department department;
    private final YearMonth month;
    private final List<HealthStaff> staff;
    private final List<StaffingRequirement> requirements;
    private final List<Unavailability> unavailabilities;
    private final Set<LocalDate> holidays;
    private final int maxWeeklyHours;

    public SchedulingContext(Department department, YearMonth month, List<HealthStaff> staff,
                             List<StaffingRequirement> requirements, List<? extends Unavailability> unavailabilities,
                             Set<LocalDate> holidays, int maxWeeklyHours) {
        if (maxWeeklyHours <= 0) {
            throw new IllegalArgumentException("Οι μέγιστες ώρες ανά εβδομάδα πρέπει να είναι θετικές.");
        }
        this.department = department;
        this.month = month;
        this.staff = Collections.unmodifiableList(new ArrayList<>(staff));
        this.requirements = Collections.unmodifiableList(new ArrayList<>(requirements));
        this.unavailabilities = Collections.unmodifiableList(new ArrayList<Unavailability>(unavailabilities));
        this.holidays = Collections.unmodifiableSet(new HashSet<>(holidays));
        this.maxWeeklyHours = maxWeeklyHours;
    }

    /** Σε άδεια, απουσία ή ώρες υποδοχής μέσα στο διάστημα [from, to); */
    public boolean isUnavailable(HealthStaff member, LocalDateTime from, LocalDateTime to) {
        for (Unavailability unavailability : unavailabilities) {
            if (unavailability.blocks(member, from, to)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWeekendOrHoliday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || holidays.contains(date);
    }

    public Department getDepartment() {
        return department;
    }

    public YearMonth getMonth() {
        return month;
    }

    public List<HealthStaff> getStaff() {
        return staff;
    }

    public List<StaffingRequirement> getRequirements() {
        return requirements;
    }

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }
}
