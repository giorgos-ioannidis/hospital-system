package gr.ihu.hospital.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.notification.Channel;
import gr.ihu.hospital.domain.notification.Notification;
import gr.ihu.hospital.domain.notification.NotificationStatus;
import gr.ihu.hospital.domain.user.Gender;
import gr.ihu.hospital.domain.user.Patient;

/** FR-31: αποτυχημένη ειδοποίηση ξαναδοκιμάζεται ανά 30 λεπτά, έως 3 προσπάθειες. */
class NotificationRetryTest {

    @Test
    void notification_retriesEvery30MinutesUpToThreeTimes() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 16, 8, 0);
        Patient patient = new Patient("p", "h", "Α", "Β", "p@test", "690", "01019000001", Gender.F, LocalDate.of(1990, 1, 1));
        Notification notification = new Notification(patient, Channel.SMS, "Υπενθύμιση", "Αύριο ραντεβού", now);

        notification.markFailed(now);
        assertThat(notification.isDue(now.plusMinutes(29))).isFalse();
        assertThat(notification.isDue(now.plusMinutes(30))).isTrue();
        notification.markFailed(now.plusMinutes(30));
        notification.markFailed(now.plusMinutes(60));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getAttempts()).isEqualTo(3);
        assertThat(notification.isDue(now.plusDays(1))).isFalse();
    }
}
