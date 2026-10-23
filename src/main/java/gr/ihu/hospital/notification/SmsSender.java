package gr.ihu.hospital.notification;

/** Αποστολή SMS (FR-31). Η μόνη υλοποίηση είναι η LoggingSmsSender. */
public interface SmsSender {

    void send(String phone, String text);
}
