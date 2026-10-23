package gr.ihu.hospital.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

/**
 * Ασφάλεια (UC-01, NFR-02): form login, bcrypt, έλεγχος ρόλου σε κάθε URL
 * (και ξανά με @PreAuthorize στα services). Ο χρήστης φορτώνεται από το AuthService.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, RecordAccessDeniedHandler deniedHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/privacy", "/webjars/**", "/css/**", "/js/**", "/img/**", "/error")
                    .permitAll()
                .requestMatchers("/staff/**", "/leave/pending", "/leave/*/approve", "/leave/*/reject",
                                 "/absences/replacements").hasRole("SUPERVISOR")
                .requestMatchers("/schedules/mine", "/leave", "/absences").hasAnyRole("DOCTOR", "NURSE")
                .requestMatchers("/schedules/**").hasRole("SUPERVISOR")
                .requestMatchers("/office-hours/**", "/appointments/doctor", "/appointments/*/status").hasRole("DOCTOR")
                .requestMatchers("/appointments/**").hasRole("PATIENT")
                .requestMatchers("/overtime/**", "/beds/**").hasRole("SUPERVISOR")
                .requestMatchers("/records/**", "/export/record/**", "/export/exam/**").hasAnyRole("DOCTOR", "NURSE", "PATIENT")
                .requestMatchers("/export/schedule").hasAnyRole("DOCTOR", "NURSE")
                .requestMatchers("/export/appointments").hasRole("PATIENT")
                .requestMatchers("/exams/mine").hasRole("PATIENT")
                .requestMatchers("/exams/**").hasRole("DOCTOR")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureHandler(failureHandler())
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .exceptionHandling(errors -> errors.accessDeniedHandler(deniedHandler));
        return http.build();
    }

    /** UC-01: κλειδωμένος ή ανενεργός λογαριασμός → ξεχωριστό μήνυμα. */
    private static ExceptionMappingAuthenticationFailureHandler failureHandler() {
        ExceptionMappingAuthenticationFailureHandler handler = new ExceptionMappingAuthenticationFailureHandler();
        Map<String, String> mappings = new HashMap<>();
        mappings.put(LockedException.class.getName(), "/login?locked");
        mappings.put(DisabledException.class.getName(), "/login?disabled");
        handler.setExceptionMappings(mappings);
        handler.setDefaultFailureUrl("/login?error");
        return handler;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
