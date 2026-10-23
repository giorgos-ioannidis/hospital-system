package gr.ihu.hospital.notification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.notification.Channel;
import gr.ihu.hospital.domain.notification.Notification;
import gr.ihu.hospital.domain.notification.NotificationStatus;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.AppointmentRepository;
import gr.ihu.hospital.repository.NotificationRepository;
import gr.ihu.hospital.repository.UserRepository;

/**
 * UC-27 Αποστολή Ειδοποίησης (FR-31). Οι ειδοποιήσεις μπαίνουν σε ουρά μέσα στην ίδια συναλλαγή με την αλλαγή,
 * και τις στέλνει το DailyReminderJob. Έτσι μια αποτυχία SMTP δεν χαλά ποτέ μια κράτηση.
 */
@Service
@Transactional
public class NotificationService {

    static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy 'ώρα' HH:mm");

    private final NotificationRepository notifications;
    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final EmailSender email;
    private final SmsSender sms;

    public NotificationService(NotificationRepository notifications, AppointmentRepository appointments,
                               UserRepository users, EmailSender email, SmsSender sms) {
        this.notifications = notifications;
        this.appointments = appointments;
        this.users = users;
        this.email = email;
        this.sms = sms;
    }

    /** Βάζει στην ουρά ένα email και, αν υπάρχει τηλέφωνο, ένα SMS. */
    public void notify(User recipient, String subject, String text) {
        LocalDateTime now = LocalDateTime.now();
        notifications.save(new Notification(recipient, Channel.EMAIL, subject, text, now));
        if (recipient.getPhone() != null && !recipient.getPhone().isBlank()) {
            notifications.save(new Notification(recipient, Channel.SMS, subject, text, now));
        }
    }

    public void notifySupervisors(String subject, String text) {
        for (User supervisor : users.findActiveSupervisors()) {
            notify(supervisor, subject, text);
        }
    }

    /** Στέλνει όσες είναι έτοιμες· αποτυχία → νέα προσπάθεια σε 30', έως 3 φορές. Επιστρέφει πόσες στάλθηκαν. */
    public int dispatchDue(LocalDateTime now) {
        int sent = 0;
        for (Notification notification : notifications.findDue(now, NotificationStatus.PENDING)) {
            try {
                deliver(notification);
                notification.markSent(now);
                sent++;
            } catch (RuntimeException e) {
                notification.markFailed(now);
            }
        }
        return sent;
    }

    /** Ο «Χρόνος»: υπενθύμιση στους ασθενείς για τα ραντεβού της ημέρας. Επιστρέφει πόσες μπήκαν στην ουρά. */
    public int remindAppointmentsOn(LocalDate day) {
        List<Appointment> due = appointments.findActiveBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay(),
                                                               AppointmentStatus.CANCELLED);
        for (Appointment appointment : due) {
            notify(appointment.getPatient(), "Υπενθύμιση ραντεβού",
                   "Σας υπενθυμίζουμε το ραντεβού σας " + appointment.getStart().format(WHEN) + " με "
                   + appointment.getDoctor().getFullName() + " (" + appointment.getRoom().getName() + ").");
        }
        return due.size();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Notification> mine(String username) {
        return notifications.findTop30ByRecipientOrderByCreatedAtDescIdDesc(users.getByUsername(username, User.class));
    }

    private void deliver(Notification notification) {
        User to = notification.getRecipient();
        if (notification.getChannel() == Channel.EMAIL) {
            email.send(to.getEmail(), notification.getSubject(), notification.getMessage());
        } else {
            sms.send(to.getPhone(), notification.getMessage());
        }
    }
}
