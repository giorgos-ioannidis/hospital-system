package gr.ihu.hospital.domain.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.HealthStaff;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Ώρες υποδοχής ενός ιατρού σε μια αίθουσα, μια συγκεκριμένη ημέρα (UC-11, FR-11).
 * Για τον αλγόριθμο βαρδιών ο ιατρός δεν είναι διαθέσιμος αυτές τις ώρες (§7.1).
 */
@Entity
public class OfficeHours implements Unavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Doctor doctor;

    @ManyToOne(optional = false)
    private Room room;

    @Column(name = "work_date")
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;
    private int slotMinutes;

    protected OfficeHours() {
    }

    public OfficeHours(Doctor doctor, Room room, LocalDate date, LocalTime startTime, LocalTime endTime,
                       int slotMinutes) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Η λήξη πρέπει να είναι μετά την έναρξη.");
        }
        if (slotMinutes < 5 || slotMinutes > 240) {
            throw new IllegalArgumentException("Η διάρκεια ραντεβού είναι από 5 έως 240 λεπτά.");
        }
        this.doctor = doctor;
        this.room = room;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotMinutes = slotMinutes;
    }

    public TimeSlot getInterval() {
        return new TimeSlot(date.atTime(startTime), date.atTime(endTime));
    }

    @Override
    public boolean blocks(HealthStaff staff, LocalDateTime from, LocalDateTime to) {
        return doctor == staff && getInterval().overlaps(from, to);
    }

    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getSlotMinutes() {
        return slotMinutes;
    }
}
