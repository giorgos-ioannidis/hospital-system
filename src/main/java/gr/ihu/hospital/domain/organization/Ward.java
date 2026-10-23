package gr.ihu.hospital.domain.organization;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Θάλαμος ενός τμήματος. Αν είναι κοντά στον σταθμό νοσηλευτών, προτιμάται για βαριά περιστατικά (FR-18). */
@Entity
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Department department;

    private String name;

    private boolean nearStation;

    protected Ward() {
    }

    public Ward(Department department, String name, boolean nearStation) {
        this.department = department;
        this.name = name;
        this.nearStation = nearStation;
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

    public boolean isNearStation() {
        return nearStation;
    }
}
