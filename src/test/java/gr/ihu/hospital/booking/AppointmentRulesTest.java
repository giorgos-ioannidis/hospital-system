package gr.ihu.hospital.booking;

import static gr.ihu.hospital.booking.BookingFixtures.DAY;
import static gr.ihu.hospital.booking.BookingFixtures.ROOM_1;
import static gr.ihu.hospital.booking.BookingFixtures.VISIT;
import static gr.ihu.hospital.booking.BookingFixtures.booked;
import static gr.ihu.hospital.booking.BookingFixtures.slot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.appointment.Rating;

/** FR-15 (κανόνας 24 ωρών) και FR-16 (κατάσταση από τον ιατρό). */
class AppointmentRulesTest {

    @Test
    void cancel_rejectedWithinLast24Hours() {
        Appointment appointment = booked(BookingFixtures.doctor("doc"), ROOM_1, VISIT, slot(10, 0, 30));

        assertThatThrownBy(() -> appointment.cancel(DAY.minusDays(1).atTime(10, 1)))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("24 ώρες");
        appointment.cancel(DAY.minusDays(1).atTime(10, 0));
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    /** UC-28: αξιολόγηση μόνο ολοκληρωμένης επίσκεψης και μόνο μία φορά. */
    @Test
    void rating_onlyOnceAndOnlyWhenCompleted() {
        Appointment appointment = booked(BookingFixtures.doctor("doc"), ROOM_1, VISIT, slot(10, 0, 30));
        assertThatThrownBy(() -> new Rating(appointment, 5, null, DAY.atTime(11, 0))).isInstanceOf(IllegalStateException.class);

        appointment.changeStatus(AppointmentStatus.COMPLETED, DAY.atTime(10, 30));
        new Rating(appointment, 4, "Πολύ καλά", DAY.atTime(11, 0));
        assertThat(appointment.getRating().getStars()).isEqualTo(4);
        assertThatThrownBy(() -> new Rating(appointment, 5, null, DAY.atTime(12, 0))).hasMessageContaining("ήδη");
    }

    @Test
    void doctorCompletesOnlyAfterStart() {
        Appointment appointment = booked(BookingFixtures.doctor("doc"), ROOM_1, VISIT, slot(10, 0, 30));

        appointment.changeStatus(AppointmentStatus.CONFIRMED, DAY.atTime(8, 0));
        assertThatThrownBy(() -> appointment.changeStatus(AppointmentStatus.COMPLETED, DAY.atTime(9, 59)))
            .isInstanceOf(IllegalStateException.class);
        appointment.changeStatus(AppointmentStatus.COMPLETED, DAY.atTime(10, 20));
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }
}
