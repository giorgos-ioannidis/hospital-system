package gr.ihu.hospital.domain.user;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Χρήστης του συστήματος (UC-01 έως UC-04). Single-table inheritance με στήλη role.
 * Κρατά τον μετρητή αποτυχημένων συνδέσεων για το κλείδωμα του λογαριασμού (UC-01).
 */
@Entity
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class User {

    /** UC-01: μετά από τόσες αποτυχίες ο λογαριασμός κλειδώνει. */
    public static final int MAX_FAILED_LOGINS = 3;

    /** Μετά από τόσα λεπτά ο λογαριασμός ξεκλειδώνει μόνος του (αλλιώς κάποιος θα κλείδωνε επίτηδες ξένους λογαριασμούς). */
    public static final int LOCK_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean enabled = true;
    private int failedAttempts;
    private boolean locked;
    private java.time.LocalDateTime lockedAt;

    protected User() {
    }

    protected User(String username, String passwordHash, String firstName, String lastName,
                   String email, String phone) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public abstract Role getRole();

    /** Καταγράφει αποτυχημένη σύνδεση. Στην 3η ο λογαριασμός κλειδώνει. */
    public void registerFailedLogin() {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_LOGINS && !locked) {
            locked = true;
            lockedAt = java.time.LocalDateTime.now();
        }
    }

    public void resetFailedLogins() {
        failedAttempts = 0;
    }

    /** Ο Προϊστάμενος ξεκλειδώνει τον λογαριασμό. */
    public void unlock() {
        locked = false;
        lockedAt = null;
        failedAttempts = 0;
    }

    /** Ξεκλειδώνει αν πέρασαν τα 15 λεπτά· επιστρέφει αν μένει κλειδωμένος. */
    public boolean isLockedAt(java.time.LocalDateTime now) {
        if (locked && lockedAt != null && !now.isBefore(lockedAt.plusMinutes(LOCK_MINUTES))) {
            unlock();
        }
        return locked;
    }

    /** UC-03: ο λογαριασμός απενεργοποιείται αντί να διαγραφεί, ώστε να μένει το ιστορικό του. */
    public void setActive(boolean active) {
        this.enabled = active;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateContact(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean isLocked() {
        return locked;
    }
}
