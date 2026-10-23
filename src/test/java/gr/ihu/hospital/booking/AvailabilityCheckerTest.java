package gr.ihu.hospital.booking;

import static gr.ihu.hospital.booking.BookingFixtures.DAY;
import static gr.ihu.hospital.booking.BookingFixtures.ECHO;
import static gr.ihu.hospital.booking.BookingFixtures.ECHO_EXAM;
import static gr.ihu.hospital.booking.BookingFixtures.ROOM_1;
import static gr.ihu.hospital.booking.BookingFixtures.ROOM_2;
import static gr.ihu.hospital.booking.BookingFixtures.VISIT;
import static gr.ihu.hospital.booking.BookingFixtures.booked;
import static gr.ihu.hospital.booking.BookingFixtures.hours;
import static gr.ihu.hospital.booking.BookingFixtures.slot;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.leave.Absence;
import gr.ihu.hospital.domain.leave.Unavailability;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.domain.user.Doctor;

/** UC-13 / FR-13: καμία διπλοκράτηση ιατρού, αίθουσας ή εξοπλισμού. */
class AvailabilityCheckerTest {

    private final Doctor doctorA = BookingFixtures.doctor("docA");
    private final Doctor doctorB = BookingFixtures.doctor("docB");
    private final List<OfficeHours> officeHours = Arrays.asList(
        hours(doctorA, ROOM_1, DAY, 9, 13), hours(doctorA, ROOM_2, DAY, 9, 13),
        hours(doctorB, ROOM_1, DAY, 9, 13), hours(doctorB, ROOM_2, DAY, 9, 13));

    /** TC-4: ο ίδιος ιατρός, η ίδια αίθουσα ή ο ίδιος εξοπλισμός δεν κλείνονται δύο φορές την ίδια ώρα. */
    @Test
    void availability_rejectsDoubleBookingOfDoctorRoomOrEquipment() {
        Appointment existing = booked(doctorA, ROOM_1, ECHO_EXAM, slot(10, 0, 30));
        AvailabilityChecker checker = checker(Collections.singletonList(existing));

        assertThat(checker.conflicts(doctorA, ROOM_2, null, slot(10, 0, 30)))
            .as("ίδιος ιατρός").containsExactly("Ο ιατρός έχει ήδη ραντεβού αυτή την ώρα.");
        assertThat(checker.conflicts(doctorB, ROOM_1, null, slot(10, 15, 30)))
            .as("ίδια αίθουσα, επικάλυψη 15'").containsExactly("Η αίθουσα Κ1 είναι ήδη κρατημένη.");
        assertThat(checker.conflicts(doctorB, ROOM_2, ECHO, slot(10, 0, 30)))
            .as("ίδιος εξοπλισμός").containsExactly("Ο εξοπλισμός «Υπερηχοκαρδιογράφος» είναι ήδη κρατημένος.");
        assertThat(checker.isFree(doctorA, ROOM_1, ECHO, slot(10, 30, 30))).as("αμέσως μετά: ελεύθερο").isTrue();
        assertThat(checker.isFree(doctorB, ROOM_2, null, slot(10, 0, 30))).as("άλλοι πόροι: ελεύθερο").isTrue();
    }

    @Test
    void availability_ignoresCancelledAppointments() {
        Appointment cancelled = booked(doctorA, ROOM_1, VISIT, slot(10, 0, 30));
        cancelled.cancelByHospital();

        assertThat(checker(Collections.singletonList(cancelled)).isFree(doctorA, ROOM_1, null, slot(10, 0, 30))).isTrue();
    }

    @Test
    void availability_rejectsOutsideOfficeHoursShiftsAndAbsences() {
        Schedule schedule = new Schedule(BookingFixtures.CARDIOLOGY, YearMonth.from(DAY), 48);
        Shift morning = schedule.addShift(doctorA.getSpecialty(), DAY, ShiftType.MORNING, 1);
        AvailabilityChecker onShift = new AvailabilityChecker(Collections.<Appointment>emptyList(),
            Collections.singletonList(new Assignment(morning, doctorA)), Collections.<Unavailability>emptyList(), officeHours);
        AvailabilityChecker absent = new AvailabilityChecker(Collections.<Appointment>emptyList(),
            Collections.<Assignment>emptyList(), Collections.singletonList(new Absence(doctorA, DAY, DAY, "γρίπη")), officeHours);

        assertThat(checker(Collections.<Appointment>emptyList()).isFree(doctorA, ROOM_1, null, slot(14, 0, 30))).isFalse();
        assertThat(onShift.conflicts(doctorA, ROOM_1, null, slot(10, 0, 30)))
            .containsExactly("Ο ιατρός έχει βάρδια ή εφημερία αυτή την ώρα.");
        assertThat(absent.conflicts(doctorA, ROOM_1, null, slot(10, 0, 30)))
            .containsExactly("Ο ιατρός είναι σε άδεια ή απουσία.");
    }

    private AvailabilityChecker checker(List<Appointment> appointments) {
        return new AvailabilityChecker(appointments, Collections.<Assignment>emptyList(),
                                       Collections.<Unavailability>emptyList(), officeHours);
    }
}
