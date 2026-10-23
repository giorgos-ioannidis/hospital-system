package gr.ihu.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.leave.LeaveRequest;
import gr.ihu.hospital.domain.leave.LeaveType;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.web.dto.LeaveForm;
import gr.ihu.hospital.web.dto.ScheduleParamsForm;

/** Η ροή της Επαύξησης 1 μέσα από τα services, με τη βάση H2: πρόγραμμα → έγκριση → άδεια → αναπλήρωση. */
@SpringBootTest
@Transactional
class Increment1FlowTest {

    private static final YearMonth NEXT_MONTH = YearMonth.now().plusMonths(1);

    @Autowired
    ScheduleService schedules;
    @Autowired
    LeaveService leaves;
    @Autowired
    AbsenceService absences;
    @Autowired
    UserRepository users;

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateApproveLeaveAndReplace() {
        loginAs("supervisor", "SUPERVISOR");
        Schedule schedule = schedules.generate(params());
        assertThat(schedule.getId()).isNotNull();
        assertThat(schedule.getShifts()).hasSize(NEXT_MONTH.lengthOfMonth() * 5);
        schedules.approve(schedule.getId(), "supervisor");
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);

        loginAs("nurse1", "NURSE");
        HealthStaff nurse = users.getByUsername("nurse1", HealthStaff.class);
        List<Assignment> before = schedules.mySchedule("nurse1", NEXT_MONTH);
        assertThat(before).isNotEmpty();
        LocalDate day = before.get(0).getShift().getDate();
        LeaveRequest request = leaves.request("nurse1", leaveOn(day));

        loginAs("supervisor", "SUPERVISOR");
        assertThat(leaves.pending()).extracting(p -> p.getRequest().getId()).contains(request.getId());
        assertThat(leaves.approve(request.getId(), "supervisor")).isPositive();
        assertThat(before.get(0).getShift().isAssigned(nurse)).isFalse();

        AbsenceService.UncoveredShift open = absences.uncovered().stream()
            .filter(u -> u.getShift().getId().equals(before.get(0).getShift().getId())).findFirst().orElseThrow();
        assertThat(open.getCandidates()).isNotEmpty().doesNotContain(nurse);
        absences.assignReplacement(open.getShift().getId(), open.getCandidates().get(0).getId());
        assertThat(open.getShift().isUnderstaffed()).isFalse();
    }

    @Test
    void nurseCannotGenerateSchedule() {
        loginAs("nurse1", "NURSE");
        assertThatThrownBy(() -> schedules.generate(params())).isInstanceOf(AccessDeniedException.class);
    }

    private static ScheduleParamsForm params() {
        ScheduleParamsForm form = new ScheduleParamsForm();
        form.setDepartmentId(1L);
        form.setMonth(NEXT_MONTH);
        return form;
    }

    private static LeaveForm leaveOn(LocalDate day) {
        LeaveForm form = new LeaveForm();
        form.setType(LeaveType.ANNUAL);
        form.setStartDate(day);
        form.setEndDate(day);
        return form;
    }

    private static void loginAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(username, null, "ROLE_" + role));
    }
}
