package gr.ihu.hospital.domain.organization;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Κλίνη ενός θαλάμου. Οι κλίνες απομόνωσης είναι σε μονόκλινα. */
@Entity
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Ward ward;

    private String label;

    private boolean isolation;

    protected Bed() {
    }

    public Bed(Ward ward, String label, boolean isolation) {
        this.ward = ward;
        this.label = label;
        this.isolation = isolation;
    }

    /** π.χ. «Θάλαμος Κ-Α / Α2». */
    public String getName() {
        return ward.getName() + " / " + label;
    }

    public Long getId() {
        return id;
    }

    public Ward getWard() {
        return ward;
    }

    public String getLabel() {
        return label;
    }

    public boolean isIsolation() {
        return isolation;
    }
}
