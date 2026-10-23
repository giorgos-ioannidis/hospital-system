package gr.ihu.hospital.service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.organization.MedicalService;
import gr.ihu.hospital.domain.record.AccessAction;
import gr.ihu.hospital.domain.record.ExamOrder;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.notification.ExamEvent;
import gr.ihu.hospital.repository.ExamOrderRepository;
import gr.ihu.hospital.repository.MedicalServiceRepository;
import gr.ihu.hospital.repository.UserRepository;

/** UC-20 Παραγγελία Εξέτασης (FR-22, κανόνας 7 ημερών) και UC-21 Προβολή Αποτελεσμάτων (FR-23). */
@Service
@Transactional
public class ExamService {

    private final ExamOrderRepository orders;
    private final MedicalServiceRepository services;
    private final UserRepository users;
    private final MedicalRecordService records;
    private final ApplicationEventPublisher events;

    public ExamService(ExamOrderRepository orders, MedicalServiceRepository services, UserRepository users,
                       MedicalRecordService records, ApplicationEventPublisher events) {
        this.orders = orders;
        this.services = services;
        this.users = users;
        this.records = records;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<MedicalService> catalog() {
        return services.findAll(org.springframework.data.domain.Sort.by("department.name", "name"));
    }

    /** Ο ιατρός παραγγέλνει εξέταση σε ασθενή που θεραπεύει· όχι ίδια εξέταση μέσα σε 7 ημέρες. */
    @PreAuthorize("hasRole('DOCTOR')")
    public ExamOrder order(String username, Long patientId, Long serviceId, String notes) {
        Doctor doctor = users.getByUsername(username, Doctor.class);
        Patient patient = records.requireView(username, patientId, AccessAction.ADD_ENTRY);
        MedicalService exam = services.findById(serviceId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        for (ExamOrder previous : orders.findByPatientOrderByOrderedAtDesc(patient)) {
            if (previous.blocksRepeatOf(exam, now)) {
                throw new IllegalArgumentException("Η εξέταση «" + exam.getName() + "» έχει ήδη παραγγελθεί για τον ασθενή"
                    + " τις τελευταίες " + ExamOrder.REPEAT_DAYS + " ημέρες.");
            }
        }
        ExamOrder order = orders.save(new ExamOrder(patient, doctor, exam, notes, now));
        events.publishEvent(new ExamEvent(order, ExamEvent.Type.ORDERED));
        return order;
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public void recordResult(String username, Long orderId, String text) {
        ExamOrder order = ownOrder(username, orderId);
        records.logAllowed(order.getDoctor(), order.getPatient(), AccessAction.ADD_ENTRY);
        order.recordResult(text, LocalDateTime.now());
        events.publishEvent(new ExamEvent(order, ExamEvent.Type.RESULT_READY));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public void cancel(String username, Long orderId) {
        ExamOrder order = ownOrder(username, orderId);
        records.logAllowed(order.getDoctor(), order.getPatient(), AccessAction.ADD_ENTRY);
        order.cancel();
    }

    /** Οι παραγγελίες του ιατρού με τα αποτελέσματά τους· καταγράφεται μία προβολή ανά ασθενή. */
    @PreAuthorize("hasRole('DOCTOR')")
    public List<ExamOrder> ordered(String username) {
        Doctor doctor = users.getByUsername(username, Doctor.class);
        List<ExamOrder> list = orders.findByDoctorOrderByOrderedAtDesc(doctor);
        Set<Patient> seen = new LinkedHashSet<>();
        for (ExamOrder order : list) {
            if (seen.add(order.getPatient())) {
                records.logAllowed(doctor, order.getPatient(), AccessAction.VIEW_RESULT);
            }
        }
        return list;
    }

    /** Τα αποτελέσματα του ασθενούς είναι κλινικά δεδομένα: η προβολή περνά από τον έλεγχο και καταγράφεται. */
    @PreAuthorize("hasRole('PATIENT')")
    public List<ExamOrder> mine(String username) {
        Patient me = users.getByUsername(username, Patient.class);
        records.requireView(username, me.getId(), AccessAction.VIEW_RESULT);
        return orders.findByPatientOrderByOrderedAtDesc(me);
    }

    /** Ένα αποτέλεσμα για προβολή/λήψη: μόνο από όσους βλέπουν τον φάκελο (καταγράφεται). */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PATIENT')")
    public ExamOrder result(String username, Long orderId) {
        ExamOrder order = orders.findById(orderId).orElseThrow();
        records.requireView(username, order.getPatient().getId(), AccessAction.VIEW_RESULT);
        return order;
    }

    private ExamOrder ownOrder(String username, Long orderId) {
        ExamOrder order = orders.findById(orderId).orElseThrow();
        if (!order.getDoctor().getUsername().equals(username)) {
            throw new AccessDeniedException("Μόνο ο ιατρός που παρήγγειλε την εξέταση καταχωρεί αποτέλεσμα.");
        }
        return order;
    }
}
