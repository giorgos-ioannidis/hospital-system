package gr.ihu.hospital.domain.record;

/** Κατάσταση παραγγελίας εξέτασης. */
public enum ExamStatus {
    ORDERED("Σε αναμονή"), COMPLETED("Ολοκληρώθηκε"), CANCELLED("Ακυρώθηκε");

    private final String label;

    ExamStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
