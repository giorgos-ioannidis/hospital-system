package gr.ihu.hospital.domain.leave;

import java.time.LocalDate;
import java.time.LocalDateTime;

import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Supervisor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Αίτηση άδειας (UC-06). Μόνο η εγκεκριμένη άδεια μπλοκάρει τον εργαζόμενο στο πρόγραμμα. */
@Entity
public class LeaveRequest implements Unavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private HealthStaff staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveType type;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "decided_by")
    private Supervisor decidedBy;

    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;

    protected LeaveRequest() {
    }

    public LeaveRequest(HealthStaff staff, LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Η λήξη της άδειας είναι πριν από την έναρξη.");
        }
        this.staff = staff;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = LeaveStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void approve(Supervisor supervisor) {
        decide(supervisor, LeaveStatus.APPROVED);
    }

    public void reject(Supervisor supervisor) {
        decide(supervisor, LeaveStatus.REJECTED);
    }

    private void decide(Supervisor supervisor, LeaveStatus decision) {
        if (status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Η αίτηση έχει ήδη απαντηθεί.");
        }
        status = decision;
        decidedBy = supervisor;
        decidedAt = LocalDateTime.now();
    }

    /** Η άδεια καλύπτει ολόκληρες ημέρες: [startDate 00:00, endDate+1 00:00). */
    @Override
    public boolean blocks(HealthStaff other, LocalDateTime from, LocalDateTime to) {
        return status == LeaveStatus.APPROVED && staff == other
            && from.isBefore(endDate.plusDays(1).atStartOfDay()) && startDate.atStartOfDay().isBefore(to);
    }

    public Long getId() {
        return id;
    }

    public HealthStaff getStaff() {
        return staff;
    }

    public LeaveType getType() {
        return type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Supervisor getDecidedBy() {
        return decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
