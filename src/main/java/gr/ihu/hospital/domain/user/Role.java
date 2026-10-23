package gr.ihu.hospital.domain.user;

/** Οι 4 ρόλοι του συστήματος (τιμή της στήλης role). */
public enum Role {
    SUPERVISOR("Προϊστάμενος/η"), DOCTOR("Ιατρός"), NURSE("Νοσηλευτής/τρια"), PATIENT("Ασθενής");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
