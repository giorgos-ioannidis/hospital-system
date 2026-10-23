package gr.ihu.hospital.web.advice;

import java.security.Principal;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

/** Τιμές που χρειάζεται το layout σε κάθε σελίδα: η τρέχουσα διαδρομή (ενεργό μενού) και αν τρέχει το demo (dev). */
@ControllerAdvice
public class LayoutAdvice {

    private final boolean demo;
    private final UserRepository users;

    public LayoutAdvice(Environment environment, UserRepository users) {
        this.demo = environment.acceptsProfiles(Profiles.of("dev"));
        this.users = users;
    }

    /** Ο συνδεδεμένος χρήστης για το μενού (ονοματεπώνυμο, αρχικά), ή null. */
    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        return principal == null ? null : users.findByUsername(principal.getName()).orElse(null);
    }

    @ModelAttribute("path")
    public String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    @ModelAttribute("demoMode")
    public boolean demoMode() {
        return demo;
    }
}
