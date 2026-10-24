package gr.ihu.hospital.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.Role;
import gr.ihu.hospital.domain.user.StaffAccountData;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.domain.user.UserFactory;
import gr.ihu.hospital.repository.DepartmentRepository;
import gr.ihu.hospital.repository.SpecialtyRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.web.dto.StaffAccountForm;
import gr.ihu.hospital.web.dto.StaffEditForm;

/** UC-03 CRUD Λογαριασμού Προσωπικού (FR-03): δημιουργία με το UserFactory, προβολή, επεξεργασία, απενεργοποίηση. */
@Service
@Transactional
public class StaffAccountService {

    private final UserRepository users;
    private final DepartmentRepository departments;
    private final SpecialtyRepository specialties;
    private final PasswordEncoder encoder;
    private final SupervisorScope scope;

    public StaffAccountService(UserRepository users, DepartmentRepository departments, SpecialtyRepository specialties,
                               PasswordEncoder encoder, SupervisorScope scope) {
        this.users = users;
        this.departments = departments;
        this.specialties = specialties;
        this.encoder = encoder;
        this.scope = scope;
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public User createStaff(StaffAccountForm form) {
        requireUnique(form.getUsername(), form.getEmail());
        Department department = departments.findById(form.getDepartmentId()).orElseThrow();
        scope.check(department);
        Specialty specialty = form.getSpecialtyId() == null ? null : specialties.findById(form.getSpecialtyId()).orElseThrow();
        if (form.getRole() != Role.SUPERVISOR && specialty == null) {
            throw new IllegalArgumentException("Ιατροί και νοσηλευτές χρειάζονται ειδικότητα.");
        }
        StaffAccountData data = new StaffAccountData(form.getUsername(), encoder.encode(form.getPassword()),
            form.getFirstName(), form.getLastName(), form.getEmail(), form.getPhone(), department, specialty);
        return users.save(UserFactory.create(form.getRole(), data));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<User> staffAccounts() {
        List<User> mine = new ArrayList<>();
        for (User user : users.findStaffAccounts()) {
            if (scope.covers(departmentOf(user))) {
                mine.add(user);
            }
        }
        return mine;
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public User staffAccount(Long id) {
        User user = users.findById(id).orElseThrow();
        if (user instanceof Patient) {
            throw new IllegalArgumentException("Οι λογαριασμοί ασθενών δεν διαχειρίζονται από εδώ.");
        }
        scope.check(departmentOf(user));
        return user;
    }

    /** UC-03 (U): στοιχεία επικοινωνίας, τμήμα και ειδικότητα. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public void updateStaff(Long id, StaffEditForm form) {
        User user = staffAccount(id);
        if (!form.getEmail().equalsIgnoreCase(user.getEmail()) && users.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("Το email χρησιμοποιείται ήδη.");
        }
        user.updateContact(form.getEmail(), form.getPhone());
        Department department = departments.findById(form.getDepartmentId()).orElseThrow();
        scope.check(department);
        if (user instanceof HealthStaff) {
            ((HealthStaff) user).reassign(department, specialtyOf(form.getSpecialtyId()));
        } else {
            ((Supervisor) user).reassign(department);
        }
    }

    /** UC-03 (D): απενεργοποίηση ή επανενεργοποίηση. Κανείς δεν απενεργοποιεί τον εαυτό του. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public void setActive(Long id, boolean active, String username) {
        User user = staffAccount(id);
        if (!active && user.getUsername().equals(username)) {
            throw new IllegalArgumentException("Δεν μπορείτε να απενεργοποιήσετε τον δικό σας λογαριασμό.");
        }
        user.setActive(active);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public void unlock(Long userId) {
        users.findById(userId).orElseThrow().unlock();
    }

    @Transactional(readOnly = true)
    public List<Department> departments() {
        List<Department> mine = new ArrayList<>();
        for (Department department : departments.findAllByOrderByNameAsc()) {
            if (scope.covers(department)) {
                mine.add(department);
            }
        }
        return mine;
    }

    @Transactional(readOnly = true)
    public List<Specialty> specialties() {
        return specialties.findAllByOrderByNameAsc();
    }

    private static Department departmentOf(User user) {
        return user instanceof HealthStaff ? ((HealthStaff) user).getDepartment() : ((Supervisor) user).getDepartment();
    }

    private Specialty specialtyOf(Long specialtyId) {
        if (specialtyId == null) {
            throw new IllegalArgumentException("Ιατροί και νοσηλευτές χρειάζονται ειδικότητα.");
        }
        return specialties.findById(specialtyId).orElseThrow();
    }

    private void requireUnique(String username, String email) {
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Το όνομα χρήστη υπάρχει ήδη.");
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Το email χρησιμοποιείται ήδη.");
        }
    }
}
