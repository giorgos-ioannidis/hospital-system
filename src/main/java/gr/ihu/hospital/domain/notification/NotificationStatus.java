package gr.ihu.hospital.domain.notification;

/** PENDING: περιμένει αποστολή ή επανάληψη. SENT: στάλθηκε. FAILED: απέτυχε και στις 3 προσπάθειες. */
public enum NotificationStatus {
    PENDING, SENT, FAILED
}
