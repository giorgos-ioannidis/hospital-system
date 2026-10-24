package gr.ihu.hospital.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.repository.UserRepository;

/** Ο συνδεδεμένος Προϊστάμενος ενεργεί μόνο στο τμήμα του· χωρίς τμήμα, σε όλο το νοσοκομείο. */
@Component
public class SupervisorScope {

    private final UserRepository users;

    public SupervisorScope(UserRepository users) {
        this.users = users;
    }

    public boolean covers(Department department) {
        return current().oversees(department);
    }

    public void check(Department department) {
        if (!covers(department)) {
            throw new AccessDeniedException("Το τμήμα αυτό ανήκει σε άλλον Προϊστάμενο.");
        }
    }

    private Supervisor current() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.getByUsername(username, Supervisor.class);
    }
}
