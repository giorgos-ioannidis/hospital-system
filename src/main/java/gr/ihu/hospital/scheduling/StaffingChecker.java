package gr.ihu.hospital.scheduling;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gr.ihu.hospital.domain.leave.LeaveRequest;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.schedule.StaffingRequirement;
import gr.ihu.hospital.domain.user.HealthStaff;

/**
 * FR-07 (§7.3): αν εγκριθεί η άδεια, ποιες βάρδιες του διαστήματος πέφτουν κάτω από την ελάχιστη στελέχωση;
 * Για κάθε ημέρα μετρά τους διαθέσιμους συναδέλφους της ίδιας ειδικότητας και τους «μοιράζει»
 * στις βάρδιες με τη σειρά των απαιτήσεων (ένα άτομο καλύπτει μία βάρδια την ημέρα).
 */
public final class StaffingChecker {

    // ponytail: μοντέλο χωρητικότητας ανά ημέρα· δεν βλέπει όριο ωρών ή ρεπό. Αρκεί για προειδοποίηση.
    public List<StaffingGap> check(LeaveRequest leave, List<HealthStaff> departmentStaff,
                                   List<StaffingRequirement> requirements, List<? extends Unavailability> existing) {
        List<StaffingGap> gaps = new ArrayList<>();
        for (LocalDate day = leave.getStartDate(); !day.isAfter(leave.getEndDate()); day = day.plusDays(1)) {
            int available = availableColleagues(leave.getStaff(), day, departmentStaff, existing);
            gaps.addAll(gapsOn(day, leave.getStaff(), available, requirements));
        }
        return gaps;
    }

    private List<StaffingGap> gapsOn(LocalDate day, HealthStaff requester, int available,
                                     List<StaffingRequirement> requirements) {
        List<StaffingGap> gaps = new ArrayList<>();
        int needed = 0;
        for (StaffingRequirement requirement : requirements) {
            if (requester.hasSpecialty(requirement.getSpecialty())) {
                needed += requirement.getMinStaff();
                if (needed > available) {
                    gaps.add(new StaffingGap(day, requirement.getShiftType(), requirement.getSpecialty()));
                }
            }
        }
        return gaps;
    }

    private int availableColleagues(HealthStaff requester, LocalDate day, List<HealthStaff> staff,
                                    List<? extends Unavailability> existing) {
        int count = 0;
        for (HealthStaff member : staff) {
            if (member != requester && member.hasSpecialty(requester.getSpecialty()) && !isBlocked(member, day, existing)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isBlocked(HealthStaff member, LocalDate day, List<? extends Unavailability> existing) {
        for (Unavailability unavailability : existing) {
            if (unavailability.blocks(member, day.atStartOfDay(), day.plusDays(1).atStartOfDay())) {
                return true;
            }
        }
        return false;
    }
}
