package gr.ihu.hospital.booking;

import java.util.List;
import java.util.function.Predicate;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.organization.Equipment;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.user.Doctor;

/** FR-13: ποιοι πόροι (ιατρός, αίθουσα, εξοπλισμός) είναι ήδη κρατημένοι από ενεργά ραντεβού σε ένα διάστημα. */
final class ResourceBookings {

    private final List<Appointment> appointments;

    ResourceBookings(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    boolean doctorBusy(Doctor doctor, TimeSlot slot) {
        return anyOverlapping(slot, a -> a.getDoctor() == doctor);
    }

    boolean roomBusy(Room room, TimeSlot slot) {
        return anyOverlapping(slot, a -> a.getRoom() == room);
    }

    boolean equipmentBusy(Equipment equipment, TimeSlot slot) {
        return anyOverlapping(slot, a -> a.getEquipment() == equipment);
    }

    private boolean anyOverlapping(TimeSlot slot, Predicate<Appointment> usesResource) {
        for (Appointment appointment : appointments) {
            if (appointment.isActive() && usesResource.test(appointment) && appointment.getSlot().overlaps(slot)) {
                return true;
            }
        }
        return false;
    }
}
