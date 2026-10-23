package gr.ihu.hospital.domain.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Supervisor;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** UC-23 / FR-25: οι υπερωρίες ενός μέλους για έναν μήνα, εγκεκριμένες ή απορριφθείσες από τον Προϊστάμενο. */
@Entity
public class Overtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private HealthStaff staff;

    private LocalDate periodStart;

    private int hours;

    @Enumerated(EnumType.STRING)
    private OvertimeStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "decided_by")
    private Supervisor decidedBy;

    private LocalDateTime decidedAt;

    protected Overtime() {
    }

    public Overtime(HealthStaff staff, YearMonth month, int hours, OvertimeStatus status, Supervisor decidedBy,
                    LocalDateTime now) {
        this.staff = staff;
        this.periodStart = month.atDay(1);
        this.hours = hours;
        this.status = status;
        this.decidedBy = decidedBy;
        this.decidedAt = now;
    }

    /** Νέα απόφαση για τον ίδιο μήνα (π.χ. άλλαξε το πρόγραμμα και ξαναϋπολογίστηκαν οι ώρες). */
    public void redecide(int newHours, OvertimeStatus newStatus, Supervisor by, LocalDateTime now) {
        this.hours = newHours;
        this.status = newStatus;
        this.decidedBy = by;
        this.decidedAt = now;
    }

    public Long getId() {
        return id;
    }

    public HealthStaff getStaff() {
        return staff;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public int getHours() {
        return hours;
    }

    public OvertimeStatus getStatus() {
        return status;
    }

    public Supervisor getDecidedBy() {
        return decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }
}
