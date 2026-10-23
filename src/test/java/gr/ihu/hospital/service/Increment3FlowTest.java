package gr.ihu.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.admin.OvertimeStatus;
import gr.ihu.hospital.domain.organization.Admission;
import gr.ihu.hospital.domain.organization.Bed;
import gr.ihu.hospital.domain.record.ExamOrder;
import gr.ihu.hospital.domain.record.ExamStatus;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.notification.NotificationService;
import gr.ihu.hospital.web.dto.ScheduleParamsForm;

/** Επαύξηση 3 με τη βάση H2: εξετάσεις (UC-20/21), κλίνες (UC-17/18), υπερωρίες (UC-23), PDF (UC-26). */
@SpringBootTest
@Transactional
class Increment3FlowTest {

    private static final long PATIENT3_CARDIOLOGY = 22L;
    private static final long BLOOD_COUNT = 7L;
    private static final long MRI = 10L;

    @Autowired
    ExamService exams;
    @Autowired
    BedService beds;
    @Autowired
    AdminService admin;
    @Autowired
    ScheduleService schedules;
    @Autowired
    ExportService export;
    @Autowired
    NotificationService notifications;

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void examOrderRepeatRuleAndResult() {
        loginAs("doctor1", "DOCTOR");
        assertThatThrownBy(() -> exams.order("doctor1", PATIENT3_CARDIOLOGY, BLOOD_COUNT, null))
            .as("η demo γενική αίματος είναι 3 ημερών").hasMessageContaining("7 ημέρες");
        ExamOrder mri = exams.order("doctor1", PATIENT3_CARDIOLOGY, MRI, "έλεγχος");
        exams.recordResult("doctor1", mri.getId(), "Χωρίς ευρήματα.");
        assertThat(mri.getStatus()).isEqualTo(ExamStatus.COMPLETED);
        assertThat(export.examResult("doctor1", mri.getId())).startsWith("%PDF".getBytes());

        loginAs("patient3", "PATIENT");
        assertThat(exams.mine("patient3")).extracting(ExamOrder::getResultText).contains("Χωρίς ευρήματα.");
        assertThat(notifications.mine("patient3")).extracting(n -> n.getSubject()).contains("Αποτέλεσμα εξέτασης");
        assertThat(export.record("patient3", PATIENT3_CARDIOLOGY)).startsWith("%PDF".getBytes());
    }

    @Test
    void bedSuggestionRespectsGenderAndAdmissionUpdatesOccupancy() {
        loginAs("supervisor", "SUPERVISOR");
        Patient woman = beds.patientByAmka("01011980002");
        List<Bed> suggestions = beds.suggest(woman, 1L, 2, false);

        assertThat(suggestions).hasSize(3).noneMatch(b -> b.getWard().getName().equals("Θάλαμος Κ-Α"));
        Admission admission = beds.admit("supervisor", "01011980002", suggestions.get(0).getId(), 2, false);
        assertThat(beds.occupancy().values().stream().flatMap(List::stream)
            .filter(w -> w.getWard() == admission.getBed().getWard()).findFirst().orElseThrow().getOccupied()).isEqualTo(1);
        assertThatThrownBy(() -> beds.suggest(woman, 1L, 2, false)).hasMessageContaining("νοσηλεύεται ήδη");
        assertThat(beds.occupancy().values().stream().flatMap(List::stream)
            .filter(w -> w.getWard().getName().equals("Θάλαμος Χ-Α")).findFirst().orElseThrow().isAlert()).isTrue();
        beds.discharge(admission.getId());
    }

    @Test
    void overtimeIsComputedFromActiveScheduleAndApproved() {
        loginAs("supervisor", "SUPERVISOR");
        YearMonth month = YearMonth.now().plusMonths(1);
        ScheduleParamsForm params = new ScheduleParamsForm();
        params.setDepartmentId(1L);
        params.setMonth(month);
        params.setMaxWeeklyHours(60);
        Schedule schedule = schedules.generate(params);
        schedules.approve(schedule.getId(), "supervisor");

        List<AdminService.OvertimeRow> rows = admin.overtime(1L, month);
        AdminService.OvertimeRow busiest = rows.stream().max((a, b) -> a.getOvertimeHours() - b.getOvertimeHours()).orElseThrow();
        assertThat(busiest.getOvertimeHours()).as("με όριο 60 ώρες κάποιος ξεπερνά τις 40").isPositive();
        assertThat(admin.decide(busiest.getStaff().getId(), month, true, "supervisor").getStatus())
            .isEqualTo(OvertimeStatus.APPROVED);
        assertThat(admin.overtime(1L, month).stream().filter(r -> r.getStaff() == busiest.getStaff()).findFirst()
            .orElseThrow().isOutdated()).isFalse();
    }

    private static void loginAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(username, null, "ROLE_" + role));
    }
}
