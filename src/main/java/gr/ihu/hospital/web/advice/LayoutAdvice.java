package gr.ihu.hospital.web.advice;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.NotificationRepository;
import gr.ihu.hospital.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Τιμές που χρειάζεται το layout σε κάθε σελίδα: η τρέχουσα διαδρομή (ενεργό μενού) και αν τρέχει το demo (dev). */
@ControllerAdvice
public class LayoutAdvice {

    private static final String SEEN = "notificationsSeenAt";

    private final boolean demo;
    private final UserRepository users;
    private final NotificationRepository notifications;

    public LayoutAdvice(Environment environment, UserRepository users, NotificationRepository notifications) {
        this.demo = environment.acceptsProfiles(Profiles.of("dev"));
        this.users = users;
        this.notifications = notifications;
    }

    /** Ο συνδεδεμένος χρήστης για το μενού (ονοματεπώνυμο, αρχικά), ή null. */
    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        return principal == null ? null : users.findByUsername(principal.getName()).orElse(null);
    }

    /** UC-27: πλήθος νέων ειδοποιήσεων για το καμπανάκι (από την τελευταία επίσκεψη στο UI-17, μέσα στη συνεδρία). */
    @ModelAttribute("unreadNotifications")
    public long unreadNotifications(Principal principal, HttpServletRequest request) {
        if (principal == null) {
            return 0;
        }
        HttpSession session = request.getSession();
        LocalDateTime now = LocalDateTime.now();
        if (path(request).startsWith("/notifications")) {
            session.setAttribute(SEEN, now);
            return 0;
        }
        Object seen = session.getAttribute(SEEN);
        LocalDateTime after = seen instanceof LocalDateTime ? (LocalDateTime) seen : now.minusDays(7);
        User user = users.findByUsername(principal.getName()).orElse(null);
        return user == null ? 0 : notifications.countByRecipientAndCreatedAtAfter(user, after);
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
