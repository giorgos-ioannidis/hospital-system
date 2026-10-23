package gr.ihu.hospital.web.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import gr.ihu.hospital.service.DashboardService;

/** UC-01: σύνδεση (UI-01a) και αρχική σελίδα ανά ρόλο (UI-01b). */
@Controller
public class AuthController {

    private static final int AFTERNOON = 12;

    private final DashboardService dashboard;

    public AuthController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/ui-01a-login";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "auth/privacy";
    }

    @GetMapping("/")
    public String home(Principal principal, Authentication authentication, Model model) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("me", dashboard.me(principal.getName()));
        model.addAttribute("greeting", LocalDateTime.now().getHour() < AFTERNOON ? "Καλημέρα" : "Καλησπέρα");
        model.addAttribute("now", LocalDateTime.now());
        if ("ROLE_SUPERVISOR".equals(role)) {
            model.addAllAttributes(dashboard.supervisor());
        } else if ("ROLE_PATIENT".equals(role)) {
            model.addAllAttributes(dashboard.patient(principal.getName()));
        } else {
            model.addAllAttributes(dashboard.staff(principal.getName()));
        }
        return "auth/ui-01b-home";
    }
}
