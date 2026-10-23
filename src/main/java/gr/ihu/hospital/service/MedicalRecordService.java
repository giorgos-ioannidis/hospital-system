package gr.ihu.hospital.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.record.AccessAction;
import gr.ihu.hospital.domain.record.AccessLog;
import gr.ihu.hospital.domain.record.EntryType;
import gr.ihu.hospital.domain.record.MedicalRecord;
import gr.ihu.hospital.domain.record.RecordAccessPolicy;
import gr.ihu.hospital.domain.record.RecordEntry;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.AccessLogRepository;
import gr.ihu.hospital.repository.AdmissionRepository;
import gr.ihu.hospital.repository.AppointmentRepository;
import gr.ihu.hospital.repository.ExamOrderRepository;
import gr.ihu.hospital.repository.MedicalRecordRepository;
import gr.ihu.hospital.repository.UserRepository;

/**
 * UC-19 Προβολή/Ενημέρωση Ιατρικού Ιστορικού (FR-20, FR-21, NFR-02).
 * Κάθε προβολή, εγγραφή και εξαγωγή περνά από το RecordAccessPolicy και γράφεται στο AccessLog, επιτυχημένη ή όχι.
 */
@Service
@Transactional
public class MedicalRecordService {

    static final int MIN_SEARCH_LETTERS = 3;

    private final RecordAccessPolicy policy = new RecordAccessPolicy();
    private final MedicalRecordRepository records;
    private final AppointmentRepository appointments;
    private final AdmissionRepository admissions;
    private final ExamOrderRepository exams;
    private final AccessLogRepository logs;
    private final UserRepository users;
    private final AccessLogger accessLogger;

    public MedicalRecordService(MedicalRecordRepository records, AppointmentRepository appointments,
                                AdmissionRepository admissions, ExamOrderRepository exams, AccessLogRepository logs,
                                UserRepository users, AccessLogger accessLogger) {
        this.records = records;
        this.appointments = appointments;
        this.admissions = admissions;
        this.exams = exams;
        this.logs = logs;
        this.users = users;
        this.accessLogger = accessLogger;
    }

    /** Αναζήτηση με ΑΜΚΑ ή επώνυμο. Το άνοιγμα του φακέλου ελέγχεται ξεχωριστά. */
    /** Ακριβής ΑΜΚΑ ή τουλάχιστον 3 γράμματα επωνύμου, ώστε να μη λιστάρονται όλοι οι ασθενείς με ένα γράμμα. */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Transactional(readOnly = true)
    public List<Patient> search(String query) {
        String q = query == null ? "" : query.trim();
        boolean amka = q.matches("\\d{11}");
        if (!amka && (q.length() < MIN_SEARCH_LETTERS || q.chars().anyMatch(Character::isDigit))) {
            return Collections.<Patient>emptyList();
        }
        return users.searchPatients(q);
    }

    /** Οι ασθενείς που «θεραπεύει» ο χρήστης: νοσηλευόμενοι στο τμήμα του και, για ιατρό, όσοι έχουν ραντεβού μαζί του. */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Transactional(readOnly = true)
    public Set<Patient> myPatients(String username) {
        HealthStaff me = users.getByUsername(username, HealthStaff.class);
        Set<Patient> patients = new LinkedHashSet<>(admissions.findAdmittedPatients(me.getDepartment()));
        if (me instanceof Doctor) {
            patients.addAll(appointments.findPatientsOf((Doctor) me, AppointmentStatus.CANCELLED));
        }
        return patients;
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PATIENT')")
    @Transactional(readOnly = true)
    public RecordView view(String username, Long patientId) {
        User viewer = users.getByUsername(username, User.class);
        Patient patient = requireView(viewer, patientId, AccessAction.VIEW);
        List<RecordEntry> entries = records.findByPatient(patient).map(MedicalRecord::getEntries)
            .orElse(Collections.<RecordEntry>emptyList());
        return new RecordView(patient, entries, exams.findByPatientOrderByOrderedAtDesc(patient),
                              admissions.findByPatientOrderByAdmittedAtDesc(patient), writableTypes(viewer, patient));
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PATIENT')")
    public RecordEntry addEntry(String username, Long patientId, EntryType type, String text) {
        User author = users.getByUsername(username, User.class);
        Patient patient = patient(patientId);
        boolean allowed = policy.canWrite(author, patient, isTreating(author, patient), type);
        accessLogger.log(author, patient, AccessAction.ADD_ENTRY, allowed);
        if (!allowed) {
            throw new RecordAccessDeniedException("Δεν επιτρέπεται να γράψετε αυτό το είδος εγγραφής στον φάκελο.");
        }
        MedicalRecord record = records.findByPatient(patient)
            .orElseGet(() -> records.save(new MedicalRecord(patient, LocalDateTime.now())));
        return record.addEntry(author, type, text, LocalDateTime.now());
    }

    /** Για άλλα services (εξαγωγή PDF, αποτελέσματα): ελέγχει, καταγράφει και επιστρέφει τον ασθενή ή πετά 403. */
    @PreAuthorize("isAuthenticated()")
    public Patient requireView(String username, Long patientId, AccessAction action) {
        return requireView(users.getByUsername(username, User.class), patientId, action);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @Transactional(readOnly = true)
    public Long ownPatientId(String username) {
        return users.getByUsername(username, Patient.class).getId();
    }

    /** Ο ασθενής βλέπει ποιος άνοιξε τον φάκελό του (διαφάνεια GDPR). */
    @PreAuthorize("hasRole('PATIENT')")
    @Transactional(readOnly = true)
    public List<AccessLog> accessLogOfMine(String username) {
        return logs.findTop50ByPatientOrderByOccurredAtDescIdDesc(users.getByUsername(username, Patient.class));
    }

    /** Ιατρός: ραντεβού ή εξέταση με τον ασθενή ή νοσηλεία στο τμήμα του. Νοσηλευτής: νοσηλεία στο τμήμα του. */
    public boolean isTreating(User user, Patient patient) {
        if (!(user instanceof HealthStaff)) {
            return false;
        }
        if (admissions.isAdmittedIn(patient, ((HealthStaff) user).getDepartment())) {
            return true;
        }
        return user instanceof Doctor && (appointments.existsActive((Doctor) user, patient, AppointmentStatus.CANCELLED)
            || exams.existsByDoctorAndPatient((Doctor) user, patient));
    }

    private Patient requireView(User viewer, Long patientId, AccessAction action) {
        Patient patient = patient(patientId);
        boolean allowed = policy.canView(viewer, patient, isTreating(viewer, patient));
        accessLogger.log(viewer, patient, action, allowed);
        if (!allowed) {
            throw new RecordAccessDeniedException("Δεν έχετε πρόσβαση σε αυτόν τον φάκελο.");
        }
        return patient;
    }

    private List<EntryType> writableTypes(User viewer, Patient patient) {
        boolean treating = isTreating(viewer, patient);
        List<EntryType> types = new ArrayList<>();
        for (EntryType type : EntryType.values()) {
            if (policy.canWrite(viewer, patient, treating, type)) {
                types.add(type);
            }
        }
        return types;
    }

    /** Ίδιο 403 για ανύπαρκτο id ή id που δεν είναι ασθενής, ώστε να μη χαρτογραφούνται οι ασθενείς από τα ids. */
    private Patient patient(Long patientId) {
        return users.findById(patientId).filter(Patient.class::isInstance).map(Patient.class::cast)
            .orElseThrow(() -> new RecordAccessDeniedException("Δεν έχετε πρόσβαση σε αυτόν τον φάκελο."));
    }

    /** Για την εξαγωγή PDF, αφού ο έλεγχος και η καταγραφή έγιναν ήδη με το requireView (μία γραμμή στο log). */
    List<RecordEntry> entriesOf(Patient patient) {
        return records.findByPatient(patient).map(MedicalRecord::getEntries).orElse(Collections.<RecordEntry>emptyList());
    }

    /** Για άλλα services: καταγράφει μια επιτρεπτή ενέργεια σε φάκελο (π.χ. αποτέλεσμα εξέτασης από τον ιατρό). */
    void logAllowed(User user, Patient patient, AccessAction action) {
        accessLogger.log(user, patient, action, true);
    }
}
