package gr.ihu.hospital.booking;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.organization.Equipment;
import gr.ihu.hospital.domain.user.Doctor;

/** UC-14 ροή 7a1: οι 3 πλησιέστερες ελεύθερες ώρες του ιατρού, όπου είναι ελεύθεροι και αίθουσα και εξοπλισμός. */
public final class AlternativeSlotFinder {

    public static final int ALTERNATIVES = 3;

    private final SlotGenerator generator = new SlotGenerator();

    public List<TimeSlot> nearest(AvailabilityChecker checker, Doctor doctor, Equipment equipment,
                                  List<OfficeHours> hours, LocalDateTime requested, LocalDateTime now) {
        List<TimeSlot> free = new ArrayList<>();
        for (OfficeHours officeHours : hours) {
            for (TimeSlot slot : generator.generate(officeHours)) {
                if (slot.getStart().isAfter(now) && checker.isFree(doctor, officeHours.getRoom(), equipment, slot)) {
                    free.add(slot);
                }
            }
        }
        free.sort(Comparator.comparingLong((TimeSlot slot) -> distance(requested, slot)).thenComparing(TimeSlot::getStart));
        return new ArrayList<>(free.subList(0, Math.min(ALTERNATIVES, free.size())));
    }

    private static long distance(LocalDateTime requested, TimeSlot slot) {
        return Math.abs(Duration.between(requested, slot.getStart()).toMinutes());
    }
}
