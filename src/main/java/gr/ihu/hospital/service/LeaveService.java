package gr.ihu.hospital.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.leave.LeaveRequest;
import gr.ihu.hospital.domain.leave.LeaveStatus;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.repository.AbsenceRepository;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.repository.LeaveRequestRepository;
import gr.ihu.hospital.repository.StaffingRequirementRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.scheduling.StaffingChecker;
import gr.ihu.hospital.scheduling.StaffingGap;
import gr.ihu.hospital.web.dto.LeaveForm;

/** UC-06 Υποβολή Αίτησης Άδειας (FR-06), UC-07 Έγκριση Αίτησης Άδειας με προειδοποίηση στελέχωσης (FR-07). */
@Service
@Transactional
public class LeaveService {

    private final StaffingChecker checker = new StaffingChecker();
    private final LeaveRequestRepository leaves;
    private final AbsenceRepository absences;
    private final HealthStaffRepository staff;
    private final StaffingRequirementRepository requirements;
    private final UserRepository users;
    private final ScheduleService schedules;

    public LeaveService(LeaveRequestRepository leaves, AbsenceRepository absences, HealthStaffRepository staff,
                        StaffingRequirementRepository requirements, UserRepository users, ScheduleService schedules) {
        this.leaves = leaves;
        this.absences = absences;
        this.staff = staff;
        this.requirements = requirements;
        this.users = users;
        this.schedules = schedules;
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public LeaveRequest request(String username, LeaveForm form) {
        HealthStaff me = users.getByUsername(username, HealthStaff.class);
        if (leaves.existsOverlapping(me, form.getStartDate(), form.getEndDate(), LeaveStatus.REJECTED)) {
            throw new IllegalArgumentException("Έχετε ήδη αίτηση άδειας που επικαλύπτει αυτές τις ημερομηνίες.");
        }
        return leaves.save(new LeaveRequest(me, form.getType(), form.getStartDate(), form.getEndDate(), form.getReason()));
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Transactional(readOnly = true)
    public List<LeaveRequest> mine(String username) {
        return leaves.findByStaffOrderByStartDateDesc(users.getByUsername(username, HealthStaff.class));
    }

    /** UI-07: εκκρεμείς αιτήσεις, η καθεμία με τις βάρδιες που θα έμεναν κάτω από την ελάχιστη στελέχωση. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<PendingLeave> pending() {
        List<PendingLeave> pending = new ArrayList<>();
        for (LeaveRequest leave : leaves.findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING)) {
            pending.add(new PendingLeave(leave, gapsFor(leave)));
        }
        return pending;
    }

    /** Εγκρίνει· αν υπάρχει πρόγραμμα, οι βάρδιες του αιτούντα γίνονται ακάλυπτες. Επιστρέφει πόσες. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public int approve(Long id, String username) {
        LeaveRequest leave = leaves.findById(id).orElseThrow();
        leave.approve(users.getByUsername(username, Supervisor.class));
        return schedules.releaseAssignments(leave.getStaff(), leave.getStartDate(), leave.getEndDate());
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public void reject(Long id, String username) {
        leaves.findById(id).orElseThrow().reject(users.getByUsername(username, Supervisor.class));
    }

    private List<StaffingGap> gapsFor(LeaveRequest leave) {
        Department department = leave.getStaff().getDepartment();
        List<Unavailability> off = new ArrayList<Unavailability>(
            leaves.findOverlapping(LeaveStatus.APPROVED, leave.getStartDate(), leave.getEndDate()));
        off.addAll(absences.findOverlapping(leave.getStartDate(), leave.getEndDate()));
        return checker.check(leave, staff.findByDepartmentOrderByLastNameAscFirstNameAsc(department),
                             requirements.findByDepartmentOrderByIdAsc(department), off);
    }

    /** Εκκρεμής αίτηση μαζί με τις προειδοποιήσεις στελέχωσης. */
    public static final class PendingLeave {

        private final LeaveRequest request;
        private final List<StaffingGap> gaps;

        PendingLeave(LeaveRequest request, List<StaffingGap> gaps) {
            this.request = request;
            this.gaps = gaps;
        }

        public LeaveRequest getRequest() {
            return request;
        }

        public List<StaffingGap> getGaps() {
            return gaps;
        }
    }
}
