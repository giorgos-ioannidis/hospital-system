package gr.ihu.hospital.domain.record;

import java.time.LocalDateTime;

import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** NFR-02: καταγραφή κάθε πρόσβασης και κάθε απόπειρας πρόσβασης σε ιατρικό φάκελο. */
@Entity
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private AccessAction action;

    private boolean allowed;

    private LocalDateTime occurredAt;

    protected AccessLog() {
    }

    public AccessLog(User user, Patient patient, AccessAction action, boolean allowed, LocalDateTime occurredAt) {
        this.user = user;
        this.patient = patient;
        this.action = action;
        this.allowed = allowed;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Patient getPatient() {
        return patient;
    }

    public AccessAction getAction() {
        return action;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
