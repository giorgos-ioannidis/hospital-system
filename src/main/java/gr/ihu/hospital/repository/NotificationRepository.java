package gr.ihu.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.notification.Notification;
import gr.ihu.hospital.domain.notification.NotificationStatus;
import gr.ihu.hospital.domain.user.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.status = :pending and n.nextAttemptAt <= :now order by n.id")
    List<Notification> findDue(LocalDateTime now, NotificationStatus pending);

    long countByRecipientAndCreatedAtAfter(User recipient, LocalDateTime after);

    List<Notification> findTop30ByRecipientOrderByCreatedAtDescIdDesc(User recipient);
}
