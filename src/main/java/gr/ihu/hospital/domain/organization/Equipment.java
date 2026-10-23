package gr.ihu.hospital.domain.organization;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Εξοπλισμός (π.χ. υπερηχογράφος). Δεσμεύεται από τα ραντεβού που τον χρειάζονται (FR-13). */
@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Department department;

    private String name;

    protected Equipment() {
    }

    public Equipment(Department department, String name) {
        this.department = department;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Department getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }
}
