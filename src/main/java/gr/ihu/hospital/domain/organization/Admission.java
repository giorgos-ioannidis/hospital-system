package gr.ihu.hospital.domain.organization;

import java.time.LocalDateTime;

import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.Supervisor;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Νοσηλεία ασθενούς σε κλίνη (UC-17). Ενεργή μέχρι το εξιτήριο. Δίνει στο προσωπικό του τμήματος πρόσβαση στον φάκελο. */
@Entity
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Patient patient;

    @ManyToOne(optional = false)
    private Bed bed;

    private int severity;

    private boolean isolation;

    private LocalDateTime admittedAt;

    private LocalDateTime dischargedAt;

    @ManyToOne
    @JoinColumn(name = "admitted_by")
    private Supervisor admittedBy;

    protected Admission() {
    }

    public Admission(Patient patient, Bed bed, BedRequest request, Supervisor admittedBy, LocalDateTime now) {
        this.patient = patient;
        this.bed = bed;
        this.severity = request.getSeverity();
        this.isolation = request.isIsolation();
        this.admittedBy = admittedBy;
        this.admittedAt = now;
    }

    public void discharge(LocalDateTime now) {
        if (!isActive()) {
            throw new IllegalStateException("Ο ασθενής έχει ήδη πάρει εξιτήριο.");
        }
        dischargedAt = now;
    }

    public boolean isActive() {
        return dischargedAt == null;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Bed getBed() {
        return bed;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isIsolation() {
        return isolation;
    }

    public LocalDateTime getAdmittedAt() {
        return admittedAt;
    }

    public LocalDateTime getDischargedAt() {
        return dischargedAt;
    }

    public Supervisor getAdmittedBy() {
        return admittedBy;
    }
}
