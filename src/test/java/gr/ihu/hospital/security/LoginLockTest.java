package gr.ihu.hospital.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** UC-01: 3 αποτυχημένες προσπάθειες κλειδώνουν τον λογαριασμό, ακόμη και για σωστό κωδικό. */
@SpringBootTest
@AutoConfigureMockMvc
class LoginLockTest {

    @Autowired
    MockMvc mvc;

    @Test
    void login_locksAfterThreeFailures() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(formLogin().user("nurse10").password("wrong")).andExpect(redirectedUrl("/login?error"));
        }
        mvc.perform(formLogin().user("nurse10").password("demo1234"))
            .andExpect(unauthenticated())
            .andExpect(redirectedUrl("/login?locked"));
    }
}
