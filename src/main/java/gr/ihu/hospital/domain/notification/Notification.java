package gr.ihu.hospital.domain.notification;

import java.time.LocalDateTime;

import gr.ihu.hospital.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Ειδοποίηση σε ουρά (UC-27). FR-31: αν αποτύχει, ξαναδοκιμάζεται ανά 30 λεπτά, έως 3 προσπάθειες συνολικά. */
@Entity
public class Notification {

    public static final int MAX_ATTEMPTS = 3;
    public static final int RETRY_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private String subject;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private int attempts;
    private LocalDateTime nextAttemptAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    protected Notification() {
    }

    public Notification(User recipient, Channel channel, String subject, String message, LocalDateTime now) {
        this.recipient = recipient;
        this.channel = channel;
        this.subject = subject;
        this.message = message;
        this.status = NotificationStatus.PENDING;
        this.nextAttemptAt = now;
        this.createdAt = now;
    }

    public boolean isDue(LocalDateTime now) {
        return status == NotificationStatus.PENDING && !nextAttemptAt.isAfter(now);
    }

    public void markSent(LocalDateTime now) {
        attempts++;
        status = NotificationStatus.SENT;
        sentAt = now;
    }

    /** Αποτυχία: νέα προσπάθεια σε 30' ή οριστική αποτυχία μετά την 3η. */
    public void markFailed(LocalDateTime now) {
        attempts++;
        if (attempts >= MAX_ATTEMPTS) {
            status = NotificationStatus.FAILED;
        } else {
            nextAttemptAt = now.plusMinutes(RETRY_MINUTES);
        }
    }

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
