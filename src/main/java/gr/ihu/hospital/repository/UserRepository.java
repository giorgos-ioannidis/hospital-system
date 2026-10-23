package gr.ihu.hospital.repository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.domain.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("select count(p) > 0 from Patient p where p.amka = :amka")
    boolean existsByAmka(String amka);

    @Query("select u from User u where type(u) <> Patient order by u.lastName, u.firstName")
    List<User> findStaffAccounts();

    /** Αναζήτηση ασθενούς με ΑΜΚΑ ή αρχή επωνύμου (UC-19, UC-17). */
    @Query("select p from Patient p where p.amka = :query or lower(p.lastName) like lower(concat(:query, '%'))"
         + " order by p.lastName, p.firstName")
    List<Patient> searchPatients(String query);

    @Query("select p from Patient p where p.amka = :amka")
    java.util.Optional<Patient> findPatientByAmka(String amka);

    @Query("select s from Supervisor s where s.enabled = true")
    List<Supervisor> findActiveSupervisors();

    /** Ο χρήστης με αυτό το username, ως συγκεκριμένος τύπος (π.χ. Supervisor). */
    default <T extends User> T getByUsername(String username, Class<T> type) {
        User user = findByUsername(username).orElseThrow(() -> new NoSuchElementException("Άγνωστος χρήστης."));
        if (!type.isInstance(user)) {
            throw new IllegalStateException("Η ενέργεια δεν επιτρέπεται για αυτόν τον ρόλο.");
        }
        return type.cast(user);
    }
}
