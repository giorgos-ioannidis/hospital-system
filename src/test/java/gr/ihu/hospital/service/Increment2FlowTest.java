package gr.ihu.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.PaymentMethod;
import gr.ihu.hospital.domain.appointment.PaymentStatus;
import gr.ihu.hospital.domain.notification.NotificationStatus;
import gr.ihu.hospital.notification.NotificationService;
import gr.ihu.hospital.web.dto.BookingForm;
import gr.ihu.hospital.web.dto.OfficeHoursForm;

/** Η ροή της Επαύξησης 2 με τη βάση H2: UC-11 → UC-13/14/16 → UC-27 → UC-15. */
@SpringBootTest
@Transactional
class Increment2FlowTest {

    /** Πέρα από τις 14 ημέρες των demo ωρών υποδοχής, ώστε να μη συγκρούεται με αυτές. */
    private static final LocalDate DAY = LocalDate.now().plusDays(20);
    private static final long CARDIOLOGY = 1L;
    private static final long ROOM_K1 = 1L;
    private static final long VISIT = 1L;
    private static final long ECHO = 2L;

    @Autowired
    OfficeHoursService officeHours;
    @Autowired
    AppointmentService appointments;
    @Autowired
    NotificationService notifications;
    @Autowired
    AppointmentManagementService manage;

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void officeHoursBookingNotificationsAndCancellation() {
        loginAs("doctor7", "DOCTOR");
        OfficeHours hours = officeHours.define("doctor7", officeHoursForm(ROOM_K1, 9, 11));
        loginAs("doctor4", "DOCTOR");
        assertThatThrownBy(() -> officeHours.define("doctor4", officeHoursForm(ROOM_K1, 10, 12)))
            .as("ίδια αίθουσα, επικάλυψη").hasMessageContaining("Η αίθουσα Ιατρείο Κ1 είναι κρατημένη");

        loginAs("patient1", "PATIENT");
        BookingForm first = booking(hours.getDoctor().getId(), VISIT, 9, 30);
        first.setPaymentMethod(PaymentMethod.CARD);
        assertThat(appointments.check(first).isAvailable()).isTrue();
        Appointment booked = appointments.book("patient1", first);
        assertThat(booked.getCode()).hasSize(8);
        assertThat(booked.getRoom().getName()).isEqualTo("Ιατρείο Κ1");
        assertThat(booked.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);

        loginAs("patient2", "PATIENT");
        BookingCheck taken = appointments.check(booking(hours.getDoctor().getId(), VISIT, 9, 30));
        assertThat(taken.getConflicts()).containsExactly("Ο ιατρός έχει ήδη ραντεβού αυτή την ώρα.",
                                                    "Η αίθουσα Ιατρείο Κ1 είναι ήδη κρατημένη.");
        assertThat(taken.getAlternatives()).hasSize(3).noneMatch(s -> s.getStart().equals(DAY.atTime(9, 30)));
        assertThatThrownBy(() -> appointments.book("patient2", booking(hours.getDoctor().getId(), VISIT, 9, 30)))
            .isInstanceOf(IllegalStateException.class);

        assertThat(notifications.mine("patient1")).extracting(n -> n.getSubject()).contains("Επιβεβαίωση ραντεβού");
        assertThat(notifications.dispatchDue(LocalDateTime.now())).isPositive();
        assertThat(notifications.mine("patient1")).allMatch(n -> n.getStatus() == NotificationStatus.SENT);

        loginAs("patient1", "PATIENT");
        manage.cancel("patient1", booked.getId());
        assertThat(booked.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void sharedEquipmentIsNotDoubleBooked() {
        loginAs("doctor7", "DOCTOR");
        OfficeHours k1 = officeHours.define("doctor7", officeHoursForm(ROOM_K1, 9, 11));
        loginAs("doctor4", "DOCTOR");
        OfficeHours k2 = officeHours.define("doctor4", officeHoursForm(2L, 9, 11));

        loginAs("patient1", "PATIENT");
        appointments.book("patient1", booking(k1.getDoctor().getId(), ECHO, 10, 0));
        BookingCheck other = appointments.check(booking(k2.getDoctor().getId(), ECHO, 10, 0));

        assertThat(other.getConflicts()).containsExactly("Ο εξοπλισμός «Υπερηχοκαρδιογράφος» είναι ήδη κρατημένος.");
        assertThat(appointments.check(booking(k2.getDoctor().getId(), VISIT, 10, 0)).isAvailable())
            .as("χωρίς εξοπλισμό, άλλος ιατρός/αίθουσα: ελεύθερο").isTrue();
    }

    @Test
    void deletingBookedOfficeHoursCancelsAppointments() {
        loginAs("doctor7", "DOCTOR");
        OfficeHours hours = officeHours.define("doctor7", officeHoursForm(ROOM_K1, 9, 11));
        loginAs("patient1", "PATIENT");
        Appointment booked = appointments.book("patient1", booking(hours.getDoctor().getId(), VISIT, 9, 0));

        loginAs("doctor7", "DOCTOR");
        assertThat(officeHours.bookedCount("doctor7", hours.getId())).isEqualTo(1);
        assertThat(officeHours.remove("doctor7", hours.getId())).isEqualTo(1);
        assertThat(booked.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(notifications.mine("doctor7")).isEmpty();
    }

    private static OfficeHoursForm officeHoursForm(long roomId, int from, int to) {
        OfficeHoursForm form = new OfficeHoursForm();
        form.setDate(DAY);
        form.setStartTime(LocalTime.of(from, 0));
        form.setEndTime(LocalTime.of(to, 0));
        form.setRoomId(roomId);
        form.setSlotMinutes(30);
        return form;
    }

    private static BookingForm booking(Long doctorId, long serviceId, int hour, int minute) {
        BookingForm form = new BookingForm();
        form.setDepartmentId(CARDIOLOGY);
        form.setDoctorId(doctorId);
        form.setServiceId(serviceId);
        form.setDate(DAY);
        form.setTime(LocalTime.of(hour, minute));
        form.setReason("έλεγχος");
        return form;
    }

    private static void loginAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(username, null, "ROLE_" + role));
    }
}
