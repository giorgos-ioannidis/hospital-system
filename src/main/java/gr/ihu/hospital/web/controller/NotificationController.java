package gr.ihu.hospital.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import gr.ihu.hospital.notification.NotificationService;

/** UC-27: οι ειδοποιήσεις που έλαβε ο χρήστης (UI-17). Χρήσιμο αφού το SMS είναι δοκιμαστικό. */
@Controller
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping("/notifications")
    public String mine(Principal principal, Model model) {
        model.addAttribute("notifications", notifications.mine(principal.getName()));
        return "notification/ui-17-notifications";
    }
}
