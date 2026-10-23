package gr.ihu.hospital.domain.user;

import gr.ihu.hospital.domain.organization.Department;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/** Προϊστάμενος: λογαριασμοί προσωπικού, πρόγραμμα, εγκρίσεις αδειών, αναπληρώσεις. */
@Entity
@DiscriminatorValue("SUPERVISOR")
public class Supervisor extends User {

    @ManyToOne
    private Department department;

    protected Supervisor() {
    }

    public Supervisor(StaffAccountData data) {
        super(data.getUsername(), data.getPasswordHash(), data.getFirstName(), data.getLastName(),
              data.getEmail(), data.getPhone());
        this.department = data.getDepartment();
    }

    public void reassign(Department newDepartment) {
        this.department = newDepartment;
    }

    @Override
    public Role getRole() {
        return Role.SUPERVISOR;
    }

    public Department getDepartment() {
        return department;
    }
}
