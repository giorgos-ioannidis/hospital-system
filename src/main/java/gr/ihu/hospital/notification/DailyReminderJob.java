package gr.ihu.hospital.notification;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Ο χρήστης «Χρόνος» του UC-27: υπενθυμίσεις κάθε πρωί στις 08:00 και αποστολή της ουράς κάθε λεπτό. */
@Component
public class DailyReminderJob {

    private final NotificationService notifications;

    public DailyReminderJob(NotificationService notifications) {
        this.notifications = notifications;
    }

    /** Υπενθύμιση για τα αυριανά ραντεβού. */
    @Scheduled(cron = "0 0 8 * * *")
    public void remindTomorrow() {
        notifications.remindAppointmentsOn(LocalDate.now().plusDays(1));
    }

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
    public void dispatch() {
        notifications.dispatchDue(LocalDateTime.now());
    }
}
