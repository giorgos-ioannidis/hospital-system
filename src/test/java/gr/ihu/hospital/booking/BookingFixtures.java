package gr.ihu.hospital.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.OfficeHours;
import gr.ihu.hospital.domain.appointment.TimeSlot;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Equipment;
import gr.ihu.hospital.domain.organization.MedicalService;
import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Gender;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.StaffAccountData;

/** Δεδομένα για τα unit tests των κρατήσεων: μόνο αντικείμενα, χωρίς Spring και βάση. */
final class BookingFixtures {

    static final LocalDate DAY = LocalDate.of(2026, 11, 16);
    static final Department CARDIOLOGY = new Department("Καρδιολογικό", 1);
    static final Room ROOM_1 = new Room(CARDIOLOGY, "Κ1");
    static final Room ROOM_2 = new Room(CARDIOLOGY, "Κ2");
    static final Equipment ECHO = new Equipment(CARDIOLOGY, "Υπερηχοκαρδιογράφος");
    static final MedicalService VISIT = new MedicalService(CARDIOLOGY, "Επίσκεψη", new BigDecimal("30.00"), null);
    static final MedicalService ECHO_EXAM = new MedicalService(CARDIOLOGY, "Υπέρηχος", new BigDecimal("60.00"), ECHO);
    static final Patient PATIENT = new Patient("p1", "hash", "Άννα", "Ασθενούς", "p1@test", null, "01019000001",
                                               Gender.F, LocalDate.of(1990, 1, 1));

    private BookingFixtures() {
    }

    static Doctor doctor(String name) {
        return new Doctor(new StaffAccountData(name, "hash", "Ιατρός", name, name + "@test", null, CARDIOLOGY,
                                               new Specialty("Καρδιολογία")));
    }

    static OfficeHours hours(Doctor doctor, Room room, LocalDate day, int fromHour, int toHour) {
        return new OfficeHours(doctor, room, day, LocalTime.of(fromHour, 0), LocalTime.of(toHour, 0), 30);
    }

    static TimeSlot slot(int hour, int minute, int minutes) {
        LocalDateTime start = DAY.atTime(hour, minute);
        return new TimeSlot(start, start.plusMinutes(minutes));
    }

    static Appointment booked(Doctor doctor, Room room, MedicalService service, TimeSlot slot) {
        return new Appointment(PATIENT, doctor, room, service, slot, "test");
    }
}
