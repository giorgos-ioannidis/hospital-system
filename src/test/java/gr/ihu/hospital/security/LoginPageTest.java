package gr.ihu.hospital.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** UC-01: η φόρμα σύνδεσης ανοίγει, οι demo χρήστες συνδέονται, τα υπόλοιπα θέλουν σύνδεση. */
@SpringBootTest
@AutoConfigureMockMvc
class LoginPageTest {

    @Autowired
    MockMvc mvc;

    @Test
    void loginPage_isPublic() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void home_redirectsAnonymousToLogin() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void demoSupervisor_canSignIn() throws Exception {
        mvc.perform(formLogin().user("supervisor").password("demo1234")).andExpect(authenticated().withRoles("SUPERVISOR"));
    }

    @Test
    void wrongPassword_isRejected() throws Exception {
        mvc.perform(formLogin().user("patient1").password("wrong")).andExpect(unauthenticated());
    }
}
