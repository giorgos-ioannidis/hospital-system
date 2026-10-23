package gr.ihu.hospital.booking;

import static gr.ihu.hospital.booking.BookingFixtures.DAY;
import static gr.ihu.hospital.booking.BookingFixtures.ROOM_1;
import static gr.ihu.hospital.booking.BookingFixtures.VISIT;
import static gr.ihu.hospital.booking.BookingFixtures.booked;
import static gr.ihu.hospital.booking.BookingFixtures.hours;
import static gr.ihu.hospital.booking.BookingFixtures.slot;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.user.Doctor;

/** UC-11: παραγωγή slots. UC-14 ροή 7a1: οι 3 πλησιέστερες ελεύθερες ώρες. */
class SlotsTest {

    private final Doctor doctor = BookingFixtures.doctor("doc");

    @Test
    void slotGenerator_splitsOfficeHoursIntoEqualSlots() {
        List<TimeSlot> slots = new SlotGenerator().generate(hours(doctor, ROOM_1, DAY, 9, 11));

        assertThat(slots).containsExactly(slot(9, 0, 30), slot(9, 30, 30), slot(10, 0, 30), slot(10, 30, 30));
        assertThat(new SlotGenerator().slotAt(hours(doctor, ROOM_1, DAY, 9, 11), DAY.atTime(9, 45))).isNull();
    }

    @Test
    void alternatives_areTheThreeNearestFreeSlots() {
        OfficeHours morning = hours(doctor, ROOM_1, DAY, 9, 13);
        List<Appointment> taken = Arrays.asList(booked(doctor, ROOM_1, VISIT, slot(10, 0, 30)),
                                                booked(doctor, ROOM_1, VISIT, slot(10, 30, 30)));
        AvailabilityChecker checker = new AvailabilityChecker(taken, Collections.<Assignment>emptyList(),
            Collections.<Unavailability>emptyList(), Collections.singletonList(morning));

        List<TimeSlot> nearest = new AlternativeSlotFinder().nearest(checker, doctor, null,
            Collections.singletonList(morning), DAY.atTime(10, 0), DAY.minusDays(1).atStartOfDay());

        assertThat(nearest).as("ισοπαλία 60': πρώτα η νωρίτερη").containsExactly(slot(9, 30, 30), slot(9, 0, 30), slot(11, 0, 30));
    }
}
