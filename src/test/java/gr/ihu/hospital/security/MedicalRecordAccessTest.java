package gr.ihu.hospital.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.record.AccessLog;
import gr.ihu.hospital.domain.record.EntryType;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.AccessLogRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.service.MedicalRecordService;

/** NFR-02 / UC-19: πρόσβαση στον ιατρικό φάκελο και καταγραφή κάθε απόπειρας. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MedicalRecordAccessTest {

    private static final long PATIENT1 = 20L;
    private static final long PATIENT2 = 21L;
    private static final long PATIENT3_CARDIOLOGY = 22L;
    private static final long PATIENT4_PATHOLOGY = 23L;

    @Autowired
    MedicalRecordService records;
    @Autowired
    AccessLogRepository logs;
    @Autowired
    UserRepository users;
    @Autowired
    MockMvc mvc;

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    /** TC-5: ο ασθενής δεν ανοίγει ξένο φάκελο (0 επιτυχημένες προσβάσεις) και η απόπειρα καταγράφεται. */
    @Test
    void medicalRecord_patientCannotAccessOtherPatientsRecord() throws Exception {
        loginAs("patient1", "PATIENT");

        assertThatThrownBy(() -> records.view("patient1", PATIENT2)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> records.addEntry("patient1", PATIENT2, EntryType.PATIENT_NOTE, "x"))
            .isInstanceOf(AccessDeniedException.class);
        mvc.perform(get("/records/" + PATIENT2).with(user("patient1").roles("PATIENT"))).andExpect(status().isForbidden());
        mvc.perform(get("/export/record/" + PATIENT2).with(user("patient1").roles("PATIENT"))).andExpect(status().isForbidden());

        loginAs("patient1", "PATIENT");
        User patient1 = users.findByUsername("patient1").orElseThrow();
        Patient patient2 = (Patient) users.findById(PATIENT2).orElseThrow();
        assertThat(logs.findByUserAndPatient(patient1, patient2))
            .as("4 απόπειρες, όλες καταγεγραμμένες και καμία επιτυχημένη").hasSize(4).noneMatch(AccessLog::isAllowed);
        assertThat(records.view("patient1", PATIENT1).getPatient().getId()).isEqualTo(PATIENT1);
    }

    /** Και οι απόπειρες που κόβονται από τον ρόλο (Προϊστάμενος σε URL φακέλου) καταγράφονται. */
    @Test
    void roleDeniedAttemptsAreLoggedToo() throws Exception {
        mvc.perform(get("/records/" + PATIENT2).with(user("supervisor").roles("SUPERVISOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/records/1").with(user("patient1").roles("PATIENT"))).andExpect(status().isForbidden());

        User supervisor = users.findByUsername("supervisor").orElseThrow();
        Patient patient2 = (Patient) users.findById(PATIENT2).orElseThrow();
        assertThat(logs.findByUserAndPatient(supervisor, patient2)).hasSize(1).noneMatch(AccessLog::isAllowed);
    }

    @Test
    void staffSeeOnlyPatientsTheyTreatAndOnlyDoctorsWrite() {
        loginAs("nurse1", "NURSE");
        assertThat(records.view("nurse1", PATIENT3_CARDIOLOGY).getEntries()).isNotEmpty();
        assertThat(records.view("nurse1", PATIENT3_CARDIOLOGY).isWritable()).isFalse();
        assertThatThrownBy(() -> records.view("nurse1", PATIENT4_PATHOLOGY)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> records.addEntry("nurse1", PATIENT3_CARDIOLOGY, EntryType.NOTE, "x"))
            .isInstanceOf(AccessDeniedException.class);

        loginAs("doctor1", "DOCTOR");
        records.addEntry("doctor1", PATIENT3_CARDIOLOGY, EntryType.NOTE, "Σταθερή κατάσταση.");
        assertThat(records.view("doctor1", PATIENT3_CARDIOLOGY).getEntries().get(0).getText()).isEqualTo("Σταθερή κατάσταση.");
        assertThatThrownBy(() -> records.view("doctor1", PATIENT2)).isInstanceOf(AccessDeniedException.class);

        loginAs("supervisor", "SUPERVISOR");
        assertThatThrownBy(() -> records.view("supervisor", PATIENT3_CARDIOLOGY)).isInstanceOf(AccessDeniedException.class);
    }

    private static void loginAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(username, null, "ROLE_" + role));
    }
}
