package gr.ihu.hospital.domain.organization;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Υπηρεσία ενός τμήματος (π.χ. Τακτική επίσκεψη, Υπέρηχος καρδιάς). Αν χρειάζεται εξοπλισμό, τον δεσμεύει. */
@Entity
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Department department;

    private String name;

    private BigDecimal price;

    @ManyToOne
    private Equipment equipment;

    protected MedicalService() {
    }

    public MedicalService(Department department, String name, BigDecimal price, Equipment equipment) {
        this.department = department;
        this.name = name;
        this.price = price;
        this.equipment = equipment;
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

    public BigDecimal getPrice() {
        return price;
    }

    /** Ο εξοπλισμός που δεσμεύει, ή null. */
    public Equipment getEquipment() {
        return equipment;
    }
}
