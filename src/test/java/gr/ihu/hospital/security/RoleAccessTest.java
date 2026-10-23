package gr.ihu.hospital.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** NFR-02: έλεγχος ρόλου σε κάθε αίτημα. */
@SpringBootTest
@AutoConfigureMockMvc
class RoleAccessTest {

    @Autowired
    MockMvc mvc;

    @Test
    void nurseCannotManageSchedulesOrStaff() throws Exception {
        mvc.perform(get("/schedules").with(user("nurse1").roles("NURSE"))).andExpect(status().isForbidden());
        mvc.perform(get("/staff").with(user("nurse1").roles("NURSE"))).andExpect(status().isForbidden());
        mvc.perform(get("/leave/pending").with(user("nurse1").roles("NURSE"))).andExpect(status().isForbidden());
    }

    @Test
    void patientCannotUseStaffPages() throws Exception {
        mvc.perform(get("/leave").with(user("patient1").roles("PATIENT"))).andExpect(status().isForbidden());
        mvc.perform(get("/schedules/mine").with(user("patient1").roles("PATIENT"))).andExpect(status().isForbidden());
    }
}
