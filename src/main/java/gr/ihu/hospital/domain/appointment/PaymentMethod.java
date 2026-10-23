package gr.ihu.hospital.domain.appointment;

/** Τρόπος πληρωμής (UC-16). Η κάρτα περνά από δοκιμαστικό gateway, καμία πραγματική χρέωση. */
public enum PaymentMethod {
    CARD("Κάρτα (δοκιμαστική)"), AT_DESK("Στο ταμείο του νοσοκομείου");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
