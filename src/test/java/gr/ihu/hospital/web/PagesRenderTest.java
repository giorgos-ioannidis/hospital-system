package gr.ihu.hospital.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** Κάθε οθόνη της Επαύξησης 1 φορτώνει χωρίς σφάλμα template για τον σωστό ρόλο. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PagesRenderTest {

    private static final UserRequestPostProcessor SUPERVISOR = user("supervisor").roles("SUPERVISOR");
    private static final UserRequestPostProcessor NURSE = user("nurse2").roles("NURSE");

    @Autowired
    MockMvc mvc;

    @Test
    void supervisorPagesRender() throws Exception {
        for (String page : new String[] {"/", "/schedules", "/staff", "/leave/pending", "/absences/replacements", "/profile"}) {
            mvc.perform(get(page).with(SUPERVISOR)).andExpect(status().isOk());
        }
        mvc.perform(get("/staff/2").with(SUPERVISOR)).andExpect(status().isOk());
        // UC-03: ο doctor2 ανήκει στο τμήμα 2, εκτός του Προϊσταμένου του τμήματος 1
        mvc.perform(get("/staff/3").with(SUPERVISOR)).andExpect(status().isForbidden());
        mvc.perform(get("/staff/3").with(user("supervisor2").roles("SUPERVISOR"))).andExpect(status().isOk());
        // UC-03: ο Προϊστάμενος του τμήματος 1 δεν παράγει πρόγραμμα για το τμήμα 2
        mvc.perform(post("/schedules").with(SUPERVISOR).with(csrf())
                .param("departmentId", "2").param("month", YearMonth.now().plusMonths(1).toString())
                .param("maxWeeklyHours", "48").param("fair", "true"))
            .andExpect(status().isForbidden());
        MvcResult generated = mvc.perform(post("/schedules").with(SUPERVISOR).with(csrf())
                .param("departmentId", "1").param("month", YearMonth.now().plusMonths(1).toString())
                .param("maxWeeklyHours", "48").param("fair", "true"))
            .andExpect(redirectedUrlPattern("/schedules/*")).andReturn();
        mvc.perform(get(generated.getResponse().getRedirectedUrl()).with(SUPERVISOR))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Χειροκίνητη αλλαγή")));
    }

    @Test
    void staffPagesRender() throws Exception {
        for (String page : new String[] {"/", "/schedules/mine", "/leave", "/absences", "/profile"}) {
            mvc.perform(get(page).with(NURSE)).andExpect(status().isOk());
        }
    }

    @Test
    void increment2PagesRender() throws Exception {
        UserRequestPostProcessor patient = user("patient1").roles("PATIENT");
        UserRequestPostProcessor doctor = user("doctor1").roles("DOCTOR");
        for (String page : new String[] {"/appointments/new", "/appointments/new?departmentId=1", "/appointments/mine",
                                         "/notifications"}) {
            mvc.perform(get(page).with(patient)).andExpect(status().isOk());
        }
        for (String page : new String[] {"/office-hours", "/appointments/doctor", "/notifications"}) {
            mvc.perform(get(page).with(doctor)).andExpect(status().isOk());
        }
        mvc.perform(get("/appointments/new?departmentId=1&serviceId=1&doctorId=2").with(patient))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"slot\"")));
        mvc.perform(get("/office-hours").with(patient)).andExpect(status().isForbidden());
        mvc.perform(get("/appointments/new").with(doctor)).andExpect(status().isForbidden());
    }

    /** UC-14: φόρμα → έλεγχος → UI-12 (διαθέσιμη) ή UI-11 με 3 εναλλακτικές (7a1). */
    @Test
    void bookingCheckShowsPreviewOrAlternatives() throws Exception {
        UserRequestPostProcessor patient = user("patient1").roles("PATIENT");
        java.time.LocalDate monday = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
        mvc.perform(post("/appointments/check").with(patient).with(csrf()).param("departmentId", "1")
                .param("serviceId", "1").param("doctorId", "2").param("date", monday.toString()).param("time", "09:30"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Προεπισκόπηση ραντεβού")));
        mvc.perform(post("/appointments/check").with(patient).with(csrf()).param("departmentId", "1")
                .param("serviceId", "1").param("doctorId", "2").param("date", monday.toString()).param("time", "15:00"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("πλησιέστερες ελεύθερες ώρες")));
    }

    @Test
    void increment3PagesRender() throws Exception {
        UserRequestPostProcessor patient = user("patient3").roles("PATIENT");
        UserRequestPostProcessor doctor = user("doctor1").roles("DOCTOR");
        UserRequestPostProcessor nurse = user("nurse1").roles("NURSE");
        for (String page : new String[] {"/beds", "/beds?amka=01011980002&departmentId=1&severity=3", "/beds/occupancy",
                                         "/overtime", "/info/map", "/info/map?room=1", "/info/directory"}) {
            mvc.perform(get(page).with(SUPERVISOR)).andExpect(status().isOk());
        }
        for (String page : new String[] {"/records", "/records?q=Δούκας", "/records/22", "/exams", "/schedules/mine"}) {
            mvc.perform(get(page).with(doctor)).andExpect(status().isOk());
        }
        mvc.perform(get("/records/22").with(nurse)).andExpect(status().isOk());
        for (String page : new String[] {"/records/22", "/records/mine/access-log", "/exams/mine", "/export/appointments"}) {
            mvc.perform(get(page).with(patient)).andExpect(status().isOk());
        }
        mvc.perform(get("/records/mine").with(patient)).andExpect(redirectedUrlPattern("/records/*"));
        mvc.perform(get("/export/schedule").with(nurse)).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/pdf"));
        mvc.perform(get("/beds").with(doctor)).andExpect(status().isForbidden());
        mvc.perform(get("/exams").with(patient)).andExpect(status().isForbidden());
    }

    @Test
    void registrationPageIsPublic() throws Exception {
        mvc.perform(get("/register")).andExpect(status().isOk());
    }
}
