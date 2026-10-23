package gr.ihu.hospital.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.beds.BedAllocator;
import gr.ihu.hospital.domain.organization.Admission;
import gr.ihu.hospital.domain.organization.Bed;
import gr.ihu.hospital.domain.organization.BedRequest;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Ward;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.repository.AdmissionRepository;
import gr.ihu.hospital.repository.BedRepository;
import gr.ihu.hospital.repository.DepartmentRepository;
import gr.ihu.hospital.repository.UserRepository;

/** UC-17 Κατανομή Κλίνης (FR-18) και UC-18 Προβολή Πληρότητας Θαλάμων (FR-19, >90% σε κόκκινο). */
@Service
@Transactional
public class BedService {

    private final BedAllocator allocator = new BedAllocator();
    private final BedRepository beds;
    private final AdmissionRepository admissions;
    private final DepartmentRepository departments;
    private final UserRepository users;

    public BedService(BedRepository beds, AdmissionRepository admissions, DepartmentRepository departments,
                      UserRepository users) {
        this.beds = beds;
        this.admissions = admissions;
        this.departments = departments;
        this.users = users;
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public Patient patientByAmka(String amka) {
        return users.findPatientByAmka(amka == null ? "" : amka.trim())
            .orElseThrow(() -> new IllegalArgumentException("Δεν βρέθηκε ασθενής με αυτόν τον ΑΜΚΑ."));
    }

    /** Η καλύτερη κλίνη και 2 εναλλακτικές· αν το τμήμα είναι γεμάτο, κλίνες άλλων τμημάτων. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<Bed> suggest(Patient patient, Long departmentId, int severity, boolean isolation) {
        if (admissions.isAdmitted(patient)) {
            throw new IllegalArgumentException(patient.getFullName() + " νοσηλεύεται ήδη.");
        }
        Department department = departments.findById(departmentId).orElseThrow();
        return allocator.suggest(new BedRequest(patient.getGender(), severity, isolation), beds.findInDepartment(department),
                                 beds.findOutsideDepartment(department), admissions.findActive());
    }

    /** Εισαγωγή σε κλίνη· οι σκληροί κανόνες ξαναελέγχονται με κλειδωμένη την κλίνη. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Admission admit(String username, String amka, Long bedId, int severity, boolean isolation) {
        Patient patient = patientByAmka(amka);
        Bed bed = beds.lockById(bedId).orElseThrow();
        BedRequest request = new BedRequest(patient.getGender(), severity, isolation);
        if (admissions.isAdmitted(patient) || !allocator.fits(request, bed, admissions.findActive())) {
            throw new IllegalStateException("Η κλίνη " + bed.getName() + " δεν είναι πλέον κατάλληλη ή ελεύθερη.");
        }
        return admissions.save(new Admission(patient, bed, request, users.getByUsername(username, Supervisor.class),
                                             LocalDateTime.now()));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public void discharge(Long admissionId) {
        admissions.findById(admissionId).orElseThrow().discharge(LocalDateTime.now());
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public List<Admission> activeAdmissions() {
        return admissions.findActive();
    }

    /** UC-18: πληρότητα ανά θάλαμο, ομαδοποιημένη ανά τμήμα. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional(readOnly = true)
    public Map<Department, List<Occupancy>> occupancy() {
        List<Admission> active = admissions.findActive();
        Map<Ward, Occupancy> byWard = new LinkedHashMap<>();
        for (Bed bed : beds.findAllOrdered()) {
            byWard.computeIfAbsent(bed.getWard(), Occupancy::new).add(isOccupied(bed, active));
        }
        Map<Department, List<Occupancy>> byDepartment = new LinkedHashMap<>();
        for (Occupancy ward : byWard.values()) {
            byDepartment.computeIfAbsent(ward.getWard().getDepartment(), d -> new ArrayList<>()).add(ward);
        }
        return byDepartment;
    }

    private static boolean isOccupied(Bed bed, List<Admission> active) {
        for (Admission admission : active) {
            if (admission.getBed() == bed) {
                return true;
            }
        }
        return false;
    }

    /** Πληρότητα ενός θαλάμου. Πάνω από 90% εμφανίζεται κόκκινο (FR-19). */
    public static final class Occupancy {

        public static final int ALERT_PERCENT = 90;

        private final Ward ward;
        private int beds;
        private int occupied;

        Occupancy(Ward ward) {
            this.ward = ward;
        }

        void add(boolean isOccupied) {
            beds++;
            occupied += isOccupied ? 1 : 0;
        }

        public Ward getWard() {
            return ward;
        }

        public int getBeds() {
            return beds;
        }

        public int getOccupied() {
            return occupied;
        }

        public int getPercent() {
            return beds == 0 ? 0 : occupied * 100 / beds;
        }

        public boolean isAlert() {
            return getPercent() > ALERT_PERCENT;
        }
    }
}
