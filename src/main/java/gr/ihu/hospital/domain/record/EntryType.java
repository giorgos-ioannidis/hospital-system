package gr.ihu.hospital.domain.record;

/** Είδος εγγραφής ιστορικού. Το PATIENT_NOTE το γράφει μόνο ο ίδιος ο ασθενής (FR-21). */
public enum EntryType {
    DIAGNOSIS("Διάγνωση"), MEDICATION("Φαρμακευτική αγωγή"), ALLERGY("Αλλεργία"), NOTE("Σημείωση ιατρού"),
    PATIENT_NOTE("Σημείωση ασθενούς");

    private final String label;

    EntryType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isClinical() {
        return this != PATIENT_NOTE;
    }
}
