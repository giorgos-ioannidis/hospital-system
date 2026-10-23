package gr.ihu.hospital.notification;

import gr.ihu.hospital.domain.appointment.Appointment;

/** Γεγονός αλλαγής ραντεβού (Observer: το δημοσιεύει το AppointmentService, το ακούει ο NotificationObserver). */
public final class AppointmentEvent {

    /** Τι συνέβη στο ραντεβού. */
    public enum Type {
        BOOKED, RESCHEDULED, CANCELLED
    }

    private final Appointment appointment;
    private final Type type;

    public AppointmentEvent(Appointment appointment, Type type) {
        this.appointment = appointment;
        this.type = type;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public Type getType() {
        return type;
    }
}
