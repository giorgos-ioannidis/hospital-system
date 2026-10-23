package gr.ihu.hospital.domain.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Ιατρός: ώρες υποδοχής, ραντεβού, εξετάσεις (επόμενες επαυξήσεις). */
@Entity
@DiscriminatorValue("DOCTOR")
public class Doctor extends HealthStaff {

    protected Doctor() {
    }

    public Doctor(StaffAccountData data) {
        super(data);
    }

    @Override
    public Role getRole() {
        return Role.DOCTOR;
    }
}
