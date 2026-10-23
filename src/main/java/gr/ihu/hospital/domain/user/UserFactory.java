package gr.ihu.hospital.domain.user;

/** Factory (FR-03): ο Προϊστάμενος δημιουργεί λογαριασμούς προσωπικού ανά ρόλο. */
public final class UserFactory {

    private UserFactory() {
    }

    public static User create(Role role, StaffAccountData data) {
        switch (role) {
            case DOCTOR:
                return new Doctor(data);
            case NURSE:
                return new Nurse(data);
            case SUPERVISOR:
                return new Supervisor(data);
            default:
                throw new IllegalArgumentException("Οι ασθενείς εγγράφονται μόνοι τους (FR-02).");
        }
    }
}
