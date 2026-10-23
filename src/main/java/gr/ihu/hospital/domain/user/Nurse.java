package gr.ihu.hospital.domain.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Νοσηλευτής/τρια. */
@Entity
@DiscriminatorValue("NURSE")
public class Nurse extends HealthStaff {

    protected Nurse() {
    }

    public Nurse(StaffAccountData data) {
        super(data);
    }

    @Override
    public Role getRole() {
        return Role.NURSE;
    }
}
