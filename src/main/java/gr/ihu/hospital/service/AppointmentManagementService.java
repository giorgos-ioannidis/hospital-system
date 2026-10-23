package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.appointment.Rating;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.notification.AppointmentEvent;
import gr.ihu.hospital.repository.AppointmentRepository;
import gr.ihu.hospital.repository.HealthStaffRepository;
import gr.ihu.hospital.repository.RatingRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.web.dto.BookingForm;

/** UC-15 RUD Ραντεβού (προβολή, αλλαγή, ακύρωση, κατάσταση) και UC-28 Αξιολόγηση Επίσκεψης. */
@Service
@Transactional
public class AppointmentManagementService {

    private final AppointmentRepository appointments;
    private final HealthStaffRepository staff;
    private final RatingRepository ratings;
    private final UserRepository users;
    private final AppointmentService booking;
    private final ApplicationEventPublisher events;

    public AppointmentManagementService(AppointmentRepository appointments, HealthStaffRepository staff,
                                        RatingRepository ratings, UserRepository users, AppointmentService booking,
                                        ApplicationEventPublisher events) {
        this.appointments = appointments;
        this.staff = staff;
        this.ratings = ratings;
        this.users = users;
        this.booking = booking;
        this.events = events;
    }

    @PreAuthorize("hasRole('PATIENT')")
    @Transactional(readOnly = true)
    public List<Appointment> mine(String username) {
        return appointments.findByPatientOrderByStartDesc(users.getByUsername(username, Patient.class));
    }

    /** FR-15: ακύρωση από τον ασθενή, έως 24 ώρες πριν. */
    @PreAuthorize("hasRole('PATIENT')")
    public void cancel(String username, Long id) {
        Appointment appointment = ownedByPatient(username, id);
        appointment.cancel(LocalDateTime.now());
        events.publishEvent(new AppointmentEvent(appointment, AppointmentEvent.Type.CANCELLED));
    }

    /** FR-15: αλλαγή ώρας με τον ίδιο ιατρό και υπηρεσία· ίδιος έλεγχος διαθεσιμότητας με την κράτηση. */
    @PreAuthorize("hasRole('PATIENT')")
    public BookingCheck reschedule(String username, Long id, LocalDate date, LocalTime time) {
        Appointment appointment = ownedByPatient(username, id);
        Doctor doctor = staff.lockDoctor(appointment.getDoctor().getId()).orElseThrow();
        BookingForm form = new BookingForm();
        form.setServiceId(appointment.getService().getId());
        form.setDate(date);
        form.setTime(time);
        BookingCheck check = booking.check(doctor, form, appointment);
        if (check.isAvailable()) {
            appointment.reschedule(check.getSlot(), check.getRoom(), LocalDateTime.now());
            events.publishEvent(new AppointmentEvent(appointment, AppointmentEvent.Type.RESCHEDULED));
        }
        return check;
    }

    /** UC-15 (ιατρός): τα ραντεβού του από την προηγούμενη εβδομάδα και μετά. */
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional(readOnly = true)
    public List<Appointment> doctorAppointments(String username) {
        return appointments.findByDoctorAndStartGreaterThanEqualOrderByStartAsc(
            users.getByUsername(username, Doctor.class), LocalDate.now().minusDays(7).atStartOfDay());
    }

    /** FR-16: ο ιατρός αλλάζει την κατάσταση μόνο στα δικά του ραντεβού. */
    @PreAuthorize("hasRole('DOCTOR')")
    public void changeStatus(String username, Long id, AppointmentStatus status) {
        Appointment appointment = appointments.findById(id).orElseThrow();
        if (!appointment.getDoctor().getUsername().equals(username)) {
            throw new AccessDeniedException("Δεν είναι δικό σας ραντεβού.");
        }
        appointment.changeStatus(status, LocalDateTime.now());
    }

    /** UC-28 / FR-32: ο ασθενής αξιολογεί μία φορά μια ολοκληρωμένη επίσκεψη. */
    @PreAuthorize("hasRole('PATIENT')")
    public Rating rate(String username, Long id, int stars, String comment) {
        return ratings.save(new Rating(ownedByPatient(username, id), stars, comment, LocalDateTime.now()));
    }

    private Appointment ownedByPatient(String username, Long id) {
        Appointment appointment = appointments.findById(id).orElseThrow();
        if (!appointment.getPatient().getUsername().equals(username)) {
            throw new AccessDeniedException("Δεν είναι δικό σας ραντεβού.");
        }
        return appointment;
    }
}
