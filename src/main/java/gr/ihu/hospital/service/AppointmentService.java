package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.booking.AlternativeSlotFinder;
import gr.ihu.hospital.booking.AvailabilityChecker;
import gr.ihu.hospital.booking.SlotGenerator;
import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.Payment;
import gr.ihu.hospital.domain.appointment.PaymentMethod;
import gr.ihu.hospital.domain.appointment.Rating;
import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.leave.LeaveStatus;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.MedicalService;
import gr.ihu.hospital.domain.schedule.ScheduleStatus;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.notification.AppointmentEvent;
import gr.ihu.hospital.payment.PaymentGateway;
import gr.ihu.hospital.repository.AbsenceRepository;
import gr.ihu.hospital.repository.AppointmentRepository;
import gr.ihu.hospital.repository.DepartmentRepository;
import gr.ihu.hospital.repository.EquipmentRepository;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.repository.LeaveRequestRepository;
import gr.ihu.hospital.repository.MedicalServiceRepository;
import gr.ihu.hospital.repository.OfficeHoursRepository;
import gr.ihu.hospital.repository.PaymentRepository;
import gr.ihu.hospital.repository.RatingRepository;
import gr.ihu.hospital.repository.ScheduleRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.web.dto.BookingForm;

/**
 * UC-13 Έλεγχος Διαθεσιμότητας, UC-14 Κράτηση Ραντεβού, UC-15 RUD Ραντεβού, UC-16 Επιλογή Τρόπου Πληρωμής, UC-28 Αξιολόγηση.
 * Η κράτηση γίνεται σε συναλλαγή με κλείδωμα ιατρού και εξοπλισμού, ώστε δύο ταυτόχρονες κρατήσεις
 * να μην πάρουν την ίδια ώρα (FR-13, NFR-03).
 */
@Service
@Transactional
public class AppointmentService {

    /** Πόσες ημέρες γύρω από τη ζητούμενη ώρα ψάχνουμε εναλλακτικές (7a1) και δείχνουμε ώρες υποδοχής. */
    static final int SEARCH_DAYS = 14;

    private final SlotGenerator slots = new SlotGenerator();
    private final AlternativeSlotFinder alternatives = new AlternativeSlotFinder();
    private final AppointmentRepository appointments;
    private final OfficeHoursRepository officeHours;
    private final HealthStaffRepository staff;
    private final MedicalServiceRepository services;
    private final DepartmentRepository departments;
    private final EquipmentRepository equipment;
    private final ScheduleRepository schedules;
    private final LeaveRequestRepository leaves;
    private final AbsenceRepository absences;
    private final PaymentRepository payments;
    private final UserRepository users;
    private final PaymentGateway gateway;
    private final RatingRepository ratings;
    private final ApplicationEventPublisher events;

    public AppointmentService(AppointmentRepository appointments, OfficeHoursRepository officeHours,
                              HealthStaffRepository staff, MedicalServiceRepository services,
                              DepartmentRepository departments, EquipmentRepository equipment,
                              ScheduleRepository schedules, LeaveRequestRepository leaves, AbsenceRepository absences,
                              PaymentRepository payments, UserRepository users, PaymentGateway gateway,
                              ApplicationEventPublisher events, RatingRepository ratings) {
        this.appointments = appointments;
        this.officeHours = officeHours;
        this.staff = staff;
        this.services = services;
        this.departments = departments;
        this.equipment = equipment;
        this.schedules = schedules;
        this.leaves = leaves;
        this.absences = absences;
        this.payments = payments;
        this.users = users;
        this.gateway = gateway;
        this.events = events;
        this.ratings = ratings;
    }

    @Transactional(readOnly = true)
    public List<Department> departments() {
        return departments.findAllByOrderByNameAsc();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @Transactional(readOnly = true)
    public List<MedicalService> services(Long departmentId) {
        return services.findByDepartmentOrderByIdAsc(departments.findById(departmentId).orElseThrow());
    }

    /** UC-14 βήμα 4: οι ιατροί του τμήματος με τις ώρες υποδοχής τους στις επόμενες ημέρες. */
    @PreAuthorize("hasRole('PATIENT')")
    @Transactional(readOnly = true)
    public Map<Doctor, List<OfficeHours>> doctorsWithHours(Long departmentId) {
        Map<Doctor, List<OfficeHours>> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (Doctor doctor : staff.findDoctors(departments.findById(departmentId).orElseThrow())) {
            result.put(doctor, officeHours.findByDoctorAndDateBetweenOrderByDateAscStartTimeAsc(doctor, today,
                                                                                                today.plusDays(SEARCH_DAYS)));
        }
        return result;
    }

    /** UC-13: είναι ελεύθεροι ιατρός, αίθουσα και εξοπλισμός; Αν όχι, οι 3 πλησιέστερες ελεύθερες ώρες. */
    @PreAuthorize("hasRole('PATIENT')")
    @Transactional(readOnly = true)
    public BookingCheck check(BookingForm form) {
        return check(staff.findById(form.getDoctorId()).filter(Doctor.class::isInstance).map(Doctor.class::cast).orElseThrow(), form, null);
    }

    /** UC-14 βήμα 9: καταχωρεί το ραντεβού και δεσμεύει τους πόρους· με «Πληρωμή τώρα» και την πληρωμή (UC-16). */
    @PreAuthorize("hasRole('PATIENT')")
    public Appointment book(String username, BookingForm form) {
        Patient patient = users.getByUsername(username, Patient.class);
        Doctor doctor = staff.lockDoctor(form.getDoctorId()).orElseThrow();
        MedicalService service = services.findById(form.getServiceId()).orElseThrow();
        if (service.getEquipment() != null) {
            equipment.lockById(service.getEquipment().getId());
        }
        BookingCheck check = check(doctor, form, null);
        if (!check.isAvailable()) {
            throw new IllegalStateException("Η ώρα δεν είναι πλέον διαθέσιμη: " + String.join(" ", check.getConflicts()));
        }
        Appointment appointment = appointments.save(
            new Appointment(patient, doctor, check.getRoom(), service, check.getSlot(), form.getReason()));
        if (form.getPaymentMethod() != null) {
            pay(appointment, form.getPaymentMethod());
        }
        events.publishEvent(new AppointmentEvent(appointment, AppointmentEvent.Type.BOOKED));
        return appointment;
    }

    /** Ο έλεγχος διαθεσιμότητας· με moving = το ραντεβού που μετακινείται (δεν συγκρούεται με τον εαυτό του). */
    BookingCheck check(Doctor doctor, BookingForm form, Appointment moving) {
        MedicalService service = services.findById(form.getServiceId()).orElseThrow();
        if (!service.getDepartment().getId().equals(doctor.getDepartment().getId())) {
            throw new IllegalArgumentException("Η υπηρεσία δεν ανήκει στο τμήμα του ιατρού.");
        }
        LocalDateTime requested = form.getDate().atTime(form.getTime());
        LocalDate from = LocalDate.now();
        List<OfficeHours> hours = officeHours.findByDoctorAndDateBetweenOrderByDateAscStartTimeAsc(doctor, from,
            form.getDate().plusDays(SEARCH_DAYS));
        AvailabilityChecker checker = checkerFor(doctor, from, form.getDate().plusDays(SEARCH_DAYS), hours, moving);
        OfficeHours chosen = hoursAt(hours, requested);
        TimeSlot slot = chosen == null ? null : slots.slotAt(chosen, requested);
        List<String> conflicts = conflicts(checker, doctor, service, chosen, slot, requested);
        List<TimeSlot> nearest = conflicts.isEmpty() ? Collections.<TimeSlot>emptyList()
            : alternatives.nearest(checker, doctor, service.getEquipment(), hours, requested, LocalDateTime.now());
        return new BookingCheck(doctor, service, chosen == null ? null : chosen.getRoom(), slot, conflicts, nearest);
    }

    private static List<String> conflicts(AvailabilityChecker checker, Doctor doctor, MedicalService service,
                                          OfficeHours chosen, TimeSlot slot, LocalDateTime requested) {
        if (!requested.isAfter(LocalDateTime.now())) {
            return Collections.singletonList("Η ώρα έχει ήδη περάσει.");
        }
        if (slot == null) {
            return Collections.singletonList("Δεν υπάρχει ώρα υποδοχής του ιατρού που να ξεκινά τότε.");
        }
        return checker.conflicts(doctor, chosen.getRoom(), service.getEquipment(), slot);
    }

    private OfficeHours hoursAt(List<OfficeHours> hours, LocalDateTime requested) {
        for (OfficeHours candidate : hours) {
            if (slots.slotAt(candidate, requested) != null) {
                return candidate;
            }
        }
        return null;
    }

    private AvailabilityChecker checkerFor(Doctor doctor, LocalDate from, LocalDate to, List<OfficeHours> hours,
                                           Appointment moving) {
        List<Appointment> booked = new ArrayList<>(appointments.findActiveBetween(from.atStartOfDay(),
            to.plusDays(1).atStartOfDay(), AppointmentStatus.CANCELLED));
        booked.remove(moving);
        List<Unavailability> off = new ArrayList<Unavailability>(leaves.findOverlapping(LeaveStatus.APPROVED, from, to));
        off.addAll(absences.findOverlapping(from, to));
        return new AvailabilityChecker(booked, schedules.findAssignments(doctor, from.minusDays(1), to, ScheduleStatus.ACTIVE),
                                       off, hours);
    }

    private void pay(Appointment appointment, PaymentMethod method) {
        Payment payment = new Payment(appointment, method);
        if (method == PaymentMethod.CARD) {
            payment.markPaid(gateway.charge(payment), LocalDateTime.now());
        }
        payments.save(payment);
    }
}
