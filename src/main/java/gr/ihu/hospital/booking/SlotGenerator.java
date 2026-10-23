package gr.ihu.hospital.booking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.TimeSlot;

/** UC-11 / FR-11: από το διάστημα των ωρών υποδοχής και τη διάρκεια ραντεβού παράγει τα TimeSlot. */
public final class SlotGenerator {

    public List<TimeSlot> generate(OfficeHours hours) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDateTime end = hours.getDate().atTime(hours.getEndTime());
        LocalDateTime start = hours.getDate().atTime(hours.getStartTime());
        while (!start.plusMinutes(hours.getSlotMinutes()).isAfter(end)) {
            slots.add(new TimeSlot(start, start.plusMinutes(hours.getSlotMinutes())));
            start = start.plusMinutes(hours.getSlotMinutes());
        }
        return slots;
    }

    /** Το slot που ξεκινά ακριβώς στην ώρα που ζητήθηκε, ή null αν η ώρα δεν είναι στο πλέγμα. */
    public TimeSlot slotAt(OfficeHours hours, LocalDateTime start) {
        for (TimeSlot slot : generate(hours)) {
            if (slot.getStart().equals(start)) {
                return slot;
            }
        }
        return null;
    }
}
