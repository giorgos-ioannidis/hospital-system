package gr.ihu.hospital.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.web.dto.ContactForm;
import gr.ihu.hospital.web.dto.PasswordForm;
import gr.ihu.hospital.web.dto.PatientRegistrationForm;
import gr.ihu.hospital.web.dto.PatientRegistrationForm;

/** UC-02 Εγγραφή Ασθενούς (FR-02) και UC-04 Διαχείριση Προφίλ (FR-04). */
@Service
@Transactional
public class AccountService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AccountService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /** Γενικό μήνυμα σε σύγκρουση, ώστε η φόρμα να μην αποκαλύπτει ποιος είναι ήδη ασθενής εδώ. */
    public Patient registerPatient(PatientRegistrationForm form) {
        if (users.existsByUsername(form.getUsername()) || users.existsByEmail(form.getEmail())
                || users.existsByAmka(form.getAmka())) {
            throw new IllegalArgumentException("Δεν ολοκληρώθηκε η εγγραφή: κάποιο από τα όνομα χρήστη, email ή ΑΜΚΑ"
                + " χρησιμοποιείται ήδη. Αν έχετε ήδη λογαριασμό, συνδεθείτε ή απευθυνθείτε στη γραμματεία.");
        }
        return users.save(new Patient(form.getUsername(), encoder.encode(form.getPassword()), form.getFirstName(),
                                      form.getLastName(), form.getEmail(), form.getPhone(), form.getAmka(),
                                      form.getGender(), form.getBirthDate()));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public User profile(String username) {
        return users.getByUsername(username, User.class);
    }

    @PreAuthorize("isAuthenticated()")
    public void updateContact(String username, ContactForm form) {
        User user = users.getByUsername(username, User.class);
        if (!form.getEmail().equalsIgnoreCase(user.getEmail()) && users.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("Το email χρησιμοποιείται ήδη.");
        }
        user.updateContact(form.getEmail(), form.getPhone());
    }

    @PreAuthorize("isAuthenticated()")
    public void changePassword(String username, PasswordForm form) {
        User user = users.getByUsername(username, User.class);
        if (!encoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Ο τρέχων κωδικός δεν είναι σωστός.");
        }
        user.changePassword(encoder.encode(form.getNewPassword()));
    }
}
