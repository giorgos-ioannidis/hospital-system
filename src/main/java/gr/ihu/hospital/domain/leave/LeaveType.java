package gr.ihu.hospital.domain.leave;

/** Είδος άδειας. */
public enum LeaveType {
    ANNUAL("Κανονική"), SICK("Αναρρωτική"), TRAINING("Εκπαιδευτική"), OTHER("Άλλη");

    private final String label;

    LeaveType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
