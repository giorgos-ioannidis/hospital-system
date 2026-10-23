package gr.ihu.hospital.domain.organization;

import gr.ihu.hospital.domain.user.Gender;

/** Κριτήρια εισαγωγής του FR-18: φύλο, βαρύτητα 1–3, ανάγκη απομόνωσης. */
public final class BedRequest {

    public static final int MAX_SEVERITY = 3;

    private final Gender gender;
    private final int severity;
    private final boolean isolation;

    public BedRequest(Gender gender, int severity, boolean isolation) {
        if (severity < 1 || severity > MAX_SEVERITY) {
            throw new IllegalArgumentException("Η βαρύτητα είναι από 1 έως 3.");
        }
        this.gender = gender;
        this.severity = severity;
        this.isolation = isolation;
    }

    public Gender getGender() {
        return gender;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isIsolation() {
        return isolation;
    }
}
