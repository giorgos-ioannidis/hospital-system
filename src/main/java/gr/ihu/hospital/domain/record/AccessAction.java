package gr.ihu.hospital.domain.record;

/** Τι προσπάθησε να κάνει ο χρήστης στον φάκελο. */
public enum AccessAction {
    VIEW("Προβολή"), ADD_ENTRY("Νέα εγγραφή"), EXPORT_PDF("Εξαγωγή PDF"), VIEW_RESULT("Αποτέλεσμα εξέτασης");

    private final String label;

    AccessAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
