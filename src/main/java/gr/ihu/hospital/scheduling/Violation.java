package gr.ihu.hospital.scheduling;

/** Παραβίαση κανόνα: σκληρή (ποτέ στο αυτόματο πρόγραμμα) ή μαλακή (μόνο προειδοποίηση). */
public final class Violation {

    private final String rule;
    private final boolean hard;
    private final String detail;

    public Violation(String rule, boolean hard, String detail) {
        this.rule = rule;
        this.hard = hard;
        this.detail = detail;
    }

    public String getRule() {
        return rule;
    }

    public boolean isHard() {
        return hard;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return rule + ": " + detail;
    }
}
