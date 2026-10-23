package gr.ihu.hospital.service;

import java.util.List;

import gr.ihu.hospital.scheduling.Violation;

/** Αποτέλεσμα χειροκίνητης αλλαγής (UC-05): αν εφαρμόστηκε και ποιες προειδοποιήσεις έδωσε το RuleChain. */
public final class ChangeResult {

    private final boolean applied;
    private final List<Violation> warnings;

    public ChangeResult(boolean applied, List<Violation> warnings) {
        this.applied = applied;
        this.warnings = warnings;
    }

    public boolean isApplied() {
        return applied;
    }

    public List<Violation> getWarnings() {
        return warnings;
    }
}
