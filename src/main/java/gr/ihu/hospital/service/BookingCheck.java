package gr.ihu.hospital.service;

import java.util.List;

import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.organization.MedicalService;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.user.Doctor;

/** Αποτέλεσμα του UC-13: διαθέσιμη ώρα, ή λόγοι μη διαθεσιμότητας και οι 3 πλησιέστερες ελεύθερες (7a1). */
public final class BookingCheck {

    private final Doctor doctor;
    private final MedicalService service;
    private final Room room;
    private final TimeSlot slot;
    private final List<String> conflicts;
    private final List<TimeSlot> alternatives;

    public BookingCheck(Doctor doctor, MedicalService service, Room room, TimeSlot slot, List<String> conflicts,
                        List<TimeSlot> alternatives) {
        this.doctor = doctor;
        this.service = service;
        this.room = room;
        this.slot = slot;
        this.conflicts = conflicts;
        this.alternatives = alternatives;
    }

    public boolean isAvailable() {
        return conflicts.isEmpty();
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public MedicalService getService() {
        return service;
    }

    public Room getRoom() {
        return room;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public List<TimeSlot> getAlternatives() {
        return alternatives;
    }
}
