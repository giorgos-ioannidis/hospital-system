package gr.ihu.hospital.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.record.AccessAction;
import gr.ihu.hospital.domain.record.AccessLog;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.AccessLogRepository;

/**
 * NFR-02: γράφει κάθε πρόσβαση στον φάκελο σε δική του συναλλαγή (REQUIRES_NEW), ώστε να μένει
 * καταγεγραμμένη και η απόπειρα που απορρίφθηκε, παρότι η εξωτερική συναλλαγή κάνει rollback.
 */
@Component
public class AccessLogger {

    private final AccessLogRepository logs;

    public AccessLogger(AccessLogRepository logs) {
        this.logs = logs;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, Patient patient, AccessAction action, boolean allowed) {
        logs.save(new AccessLog(user, patient, action, allowed, LocalDateTime.now()));
    }
}
