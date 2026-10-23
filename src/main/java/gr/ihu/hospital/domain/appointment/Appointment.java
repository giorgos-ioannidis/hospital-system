package gr.ihu.hospital.domain.appointment;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import gr.ihu.hospital.domain.organization.Equipment;
import gr.ihu.hospital.domain.organization.MedicalService;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

/** Ραντεβού (UC-14, UC-15): δεσμεύει ιατρό, αίθουσα και, αν χρειάζεται, εξοπλισμό. */
@Entity
public class Appointment {

    /** FR-15: ακύρωση ή αλλαγή μόνο αν απομένουν τουλάχιστον τόσες ώρες. */
    public static final int CHANGE_DEADLINE_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @ManyToOne(optional = false)
    private Patient patient;

    @ManyToOne(optional = false)
    private Doctor doctor;

    @ManyToOne(optional = false)
    private Room room;

    @ManyToOne
    private Equipment equipment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id")
    private MedicalService service;

    @Column(name = "start_at")
    private LocalDateTime start;

    @Column(name = "end_at")
    private LocalDateTime end;

    private String reason;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "appointment")
    private Payment payment;

    @OneToOne(mappedBy = "appointment")
    private Rating rating;

    protected Appointment() {
    }

    public Appointment(Patient patient, Doctor doctor, Room room, MedicalService service, TimeSlot slot,
                       String reason) {
        this.code = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        this.patient = patient;
        this.doctor = doctor;
        this.room = room;
        this.service = service;
        this.equipment = service.getEquipment();
        this.start = slot.getStart();
        this.end = slot.getEnd();
        this.reason = reason;
        this.status = AppointmentStatus.BOOKED;
        this.createdAt = LocalDateTime.now();
    }

    /** FR-15: ο ασθενής ακυρώνει, μόνο αν απομένουν ≥ 24 ώρες. */
    public void cancel(LocalDateTime now) {
        requireChangeable(now);
        status = AppointmentStatus.CANCELLED;
    }

    /** Ακύρωση από το νοσοκομείο (π.χ. διαγραφή ωρών υποδοχής, §7.5), χωρίς τον κανόνα των 24 ωρών. */
    public void cancelByHospital() {
        status = AppointmentStatus.CANCELLED;
    }

    /** FR-15: αλλαγή ώρας/αίθουσας· ο έλεγχος διαθεσιμότητας γίνεται πριν, από το AvailabilityChecker. */
    public void reschedule(TimeSlot slot, Room newRoom, LocalDateTime now) {
        requireChangeable(now);
        this.start = slot.getStart();
        this.end = slot.getEnd();
        this.room = newRoom;
    }

    /** FR-16: ο ιατρός αλλάζει την κατάσταση. Ολοκλήρωση ή μη προσέλευση μόνο αφού ξεκινήσει το ραντεβού. */
    public void changeStatus(AppointmentStatus newStatus, LocalDateTime now) {
        status.checkTransitionTo(newStatus, !now.isBefore(start));
        status = newStatus;
    }

    private void requireChangeable(LocalDateTime now) {
        if (!status.isChangeable()) {
            throw new IllegalStateException("Το ραντεβού δεν μπορεί πλέον να αλλάξει.");
        }
        if (now.plusHours(CHANGE_DEADLINE_HOURS).isAfter(start)) {
            throw new IllegalStateException("Ακύρωση ή αλλαγή επιτρέπεται έως 24 ώρες πριν από το ραντεβού.");
        }
    }

    /** Δεσμεύει πόρους; (όλα εκτός από τα ακυρωμένα) */
    public boolean isActive() {
        return status != AppointmentStatus.CANCELLED;
    }

    public TimeSlot getSlot() {
        return new TimeSlot(start, end);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Room getRoom() {
        return room;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public MedicalService getService() {
        return service;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getReason() {
        return reason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Καλείται από τον constructor της Payment, ώστε οι δύο πλευρές της σχέσης να συμφωνούν. */
    void attach(Payment newPayment) {
        if (payment != null) {
            throw new IllegalStateException("Το ραντεβού έχει ήδη πληρωμή.");
        }
        this.payment = newPayment;
    }

    /** Καλείται από τον constructor της Rating (UC-28): μία αξιολόγηση ανά επίσκεψη. */
    void attach(Rating newRating) {
        if (rating != null) {
            throw new IllegalStateException("Η επίσκεψη έχει ήδη αξιολογηθεί.");
        }
        this.rating = newRating;
    }

    /** Η αξιολόγηση της επίσκεψης, ή null. */
    public Rating getRating() {
        return rating;
    }

    /** Η πληρωμή του ραντεβού (UC-16), ή null αν δεν επιλέχθηκε «Πληρωμή τώρα». */
    public Payment getPayment() {
        return payment;
    }
}
