package gr.ihu.hospital.booking;

import java.util.ArrayList;
import java.util.List;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.organization.Equipment;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.user.Doctor;

/**
 * UC-13 Έλεγχος Διαθεσιμότητας (FR-13): είναι ελεύθεροι ιατρός, αίθουσα και εξοπλισμός σε ένα διάστημα;
 * Ελέγχει επικάλυψη με ραντεβού, βάρδιες/εφημερίες και άδειες/απουσίες, και ότι η ώρα είναι μέσα στις ώρες υποδοχής.
 * Παίρνει τα δεδομένα ως αντικείμενα, ώστε να ελέγχεται χωρίς βάση.
 */
public final class AvailabilityChecker {

    private final ResourceBookings bookings;
    private final List<Assignment> doctorShifts;
    private final List<? extends Unavailability> doctorAbsences;
    private final List<OfficeHours> officeHours;

    public AvailabilityChecker(List<Appointment> appointments, List<Assignment> doctorShifts,
                               List<? extends Unavailability> doctorAbsences, List<OfficeHours> officeHours) {
        this.bookings = new ResourceBookings(appointments);
        this.doctorShifts = doctorShifts;
        this.doctorAbsences = doctorAbsences;
        this.officeHours = officeHours;
    }

    public boolean isFree(Doctor doctor, Room room, Equipment equipment, TimeSlot slot) {
        return conflicts(doctor, room, equipment, slot).isEmpty();
    }

    /** Οι λόγοι για τους οποίους η ώρα δεν είναι διαθέσιμη (κενή λίστα = ελεύθερη). */
    public List<String> conflicts(Doctor doctor, Room room, Equipment equipment, TimeSlot slot) {
        List<String> found = new ArrayList<>();
        if (!withinOfficeHours(doctor, room, slot)) {
            found.add("Η ώρα είναι εκτός των ωρών υποδοχής του ιατρού.");
        }
        if (bookings.doctorBusy(doctor, slot)) {
            found.add("Ο ιατρός έχει ήδη ραντεβού αυτή την ώρα.");
        }
        if (bookings.roomBusy(room, slot)) {
            found.add("Η αίθουσα " + room.getName() + " είναι ήδη κρατημένη.");
        }
        if (equipment != null && bookings.equipmentBusy(equipment, slot)) {
            found.add("Ο εξοπλισμός «" + equipment.getName() + "» είναι ήδη κρατημένος.");
        }
        if (onShift(slot)) {
            found.add("Ο ιατρός έχει βάρδια ή εφημερία αυτή την ώρα.");
        }
        if (absent(doctor, slot)) {
            found.add("Ο ιατρός είναι σε άδεια ή απουσία.");
        }
        return found;
    }

    private boolean withinOfficeHours(Doctor doctor, Room room, TimeSlot slot) {
        for (OfficeHours hours : officeHours) {
            if (hours.getDoctor() == doctor && hours.getRoom() == room && hours.getInterval().contains(slot)) {
                return true;
            }
        }
        return false;
    }

    /** Οι βάρδιες που δίνονται είναι ήδη του συγκεκριμένου ιατρού. */
    private boolean onShift(TimeSlot slot) {
        for (Assignment assignment : doctorShifts) {
            if (slot.overlaps(assignment.getShift().getStart(), assignment.getShift().getEnd())) {
                return true;
            }
        }
        return false;
    }

    private boolean absent(Doctor doctor, TimeSlot slot) {
        for (Unavailability absence : doctorAbsences) {
            if (absence.blocks(doctor, slot.getStart(), slot.getEnd())) {
                return true;
            }
        }
        return false;
    }
}
