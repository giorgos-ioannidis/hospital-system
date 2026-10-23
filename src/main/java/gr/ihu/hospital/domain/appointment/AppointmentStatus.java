package gr.ihu.hospital.domain.appointment;

/** Κατάσταση ραντεβού (FR-16). Το CANCELLED δεν δεσμεύει πόρους. */
public enum AppointmentStatus {
    BOOKED("Κλεισμένο"), CONFIRMED("Επιβεβαιωμένο"), COMPLETED("Ολοκληρώθηκε"), NO_SHOW("Δεν προσήλθε"),
    CANCELLED("Ακυρώθηκε");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** FR-15: αλλαγή ή ακύρωση μόνο όσο το ραντεβού είναι κλεισμένο ή επιβεβαιωμένο. */
    public boolean isChangeable() {
        return this == BOOKED || this == CONFIRMED;
    }

    /** FR-16: επιτρεπτές μεταβάσεις από τον ιατρό· ολοκλήρωση ή μη προσέλευση μόνο αφού ξεκινήσει το ραντεβού. */
    public void checkTransitionTo(AppointmentStatus next, boolean started) {
        if (this == CANCELLED || next == CANCELLED || next == BOOKED) {
            throw new IllegalStateException("Μη επιτρεπτή αλλαγή κατάστασης.");
        }
        if (next != CONFIRMED && !started) {
            throw new IllegalStateException("Το ραντεβού δεν έχει ξεκινήσει ακόμη.");
        }
    }
}
