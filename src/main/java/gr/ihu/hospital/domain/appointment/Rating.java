package gr.ihu.hospital.domain.appointment;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/** UC-28 / FR-32: αξιολόγηση 1–5 μιας ολοκληρωμένης επίσκεψης, μία φορά. */
@Entity
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private Appointment appointment;

    private int stars;

    private String comment;

    private LocalDateTime createdAt;

    protected Rating() {
    }

    public Rating(Appointment appointment, int stars, String comment, LocalDateTime now) {
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Αξιολογείται μόνο ολοκληρωμένη επίσκεψη.");
        }
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Η αξιολόγηση είναι από 1 έως 5.");
        }
        this.appointment = appointment;
        this.stars = stars;
        this.comment = comment;
        this.createdAt = now;
        appointment.attach(this);
    }

    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public int getStars() {
        return stars;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
