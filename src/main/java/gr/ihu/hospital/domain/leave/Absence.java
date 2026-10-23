package gr.ihu.hospital.domain.leave;

import java.time.LocalDate;
import java.time.LocalDateTime;

import gr.ihu.hospital.domain.user.HealthStaff;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Δήλωση ασθένειας / απουσίας (UC-09, FR-09). Ισχύει αμέσως, χωρίς έγκριση. */
@Entity
public class Absence implements Unavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private HealthStaff staff;

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LocalDateTime declaredAt;

    protected Absence() {
    }

    public Absence(HealthStaff staff, LocalDate startDate, LocalDate endDate, String reason) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Η λήξη της απουσίας είναι πριν από την έναρξη.");
        }
        this.staff = staff;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.declaredAt = LocalDateTime.now();
    }

    /** Η απουσία καλύπτει ολόκληρες ημέρες: [startDate 00:00, endDate+1 00:00). */
    @Override
    public boolean blocks(HealthStaff other, LocalDateTime from, LocalDateTime to) {
        return staff == other
            && from.isBefore(endDate.plusDays(1).atStartOfDay()) && startDate.atStartOfDay().isBefore(to);
    }

    public Long getId() {
        return id;
    }

    public HealthStaff getStaff() {
        return staff;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getDeclaredAt() {
        return declaredAt;
    }
}
