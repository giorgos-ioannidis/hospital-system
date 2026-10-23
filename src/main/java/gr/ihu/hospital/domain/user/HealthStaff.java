package gr.ihu.hospital.domain.user;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/** Υγειονομικό προσωπικό: μπαίνει στο πρόγραμμα, παίρνει άδειες, δηλώνει απουσίες. */
@Entity
public abstract class HealthStaff extends User {

    @ManyToOne
    private Department department;

    @ManyToOne
    private Specialty specialty;

    protected HealthStaff() {
    }

    protected HealthStaff(StaffAccountData data) {
        super(data.getUsername(), data.getPasswordHash(), data.getFirstName(), data.getLastName(),
              data.getEmail(), data.getPhone());
        this.department = data.getDepartment();
        this.specialty = data.getSpecialty();
    }

    /** UC-03: μετακίνηση σε άλλο τμήμα ή αλλαγή ειδικότητας. */
    public void reassign(Department newDepartment, Specialty newSpecialty) {
        this.department = newDepartment;
        this.specialty = newSpecialty;
    }

    public boolean hasSpecialty(Specialty other) {
        return specialty != null && specialty.equals(other);
    }

    public Department getDepartment() {
        return department;
    }

    public Specialty getSpecialty() {
        return specialty;
    }
}
