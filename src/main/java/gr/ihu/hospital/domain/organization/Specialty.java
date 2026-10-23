package gr.ihu.hospital.domain.organization;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Ειδικότητα (π.χ. Καρδιολογία, Νοσηλευτική). Δύο ειδικότητες είναι ίσες αν έχουν το ίδιο μοναδικό όνομα. */
@Entity
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected Specialty() {
    }

    public Specialty(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Specialty && Objects.equals(name, ((Specialty) other).name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
