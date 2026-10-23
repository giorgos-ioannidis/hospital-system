package gr.ihu.hospital.domain.organization;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Αίθουσα/ιατρείο ενός τμήματος. Δεσμεύεται από ώρες υποδοχής και ραντεβού (FR-13). */
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Department department;

    private String name;

    protected Room() {
    }

    public Room(Department department, String name) {
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
