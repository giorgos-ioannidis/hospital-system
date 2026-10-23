package gr.ihu.hospital.domain.schedule;

import gr.ihu.hospital.domain.user.HealthStaff;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Ανάθεση ενός μέλους του προσωπικού σε μια βάρδια. */
@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Shift shift;

    @ManyToOne(optional = false)
    private HealthStaff staff;

    protected Assignment() {
    }

    public Assignment(Shift shift, HealthStaff staff) {
        this.shift = shift;
        this.staff = staff;
    }

    public Long getId() {
        return id;
    }

    public Shift getShift() {
        return shift;
    }

    public HealthStaff getStaff() {
        return staff;
    }
}
