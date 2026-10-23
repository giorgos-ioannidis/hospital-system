package gr.ihu.hospital.notification;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.schedule.Assignment;

/**
 * Observer: ακούει τις αλλαγές ραντεβού και προγράμματος και ειδοποιεί όποιον αφορούν.
 * Τα services δεν ξέρουν τίποτα για email/SMS· απλώς δημοσιεύουν γεγονότα.
 */
@Component
public class NotificationObserver {

    private final NotificationService notifications;

    public NotificationObserver(NotificationService notifications) {
        this.notifications = notifications;
    }

    @EventListener
    public void onAppointment(AppointmentEvent event) {
        Appointment appointment = event.getAppointment();
        String when = appointment.getStart().format(NotificationService.WHEN);
        notifications.notify(appointment.getPatient(), subjectOf(event.getType()),
            textOf(event.getType()) + " " + when + " με " + appointment.getDoctor().getFullName()
            + " (" + appointment.getService().getName() + "). Κωδικός: " + appointment.getCode());
    }

    @EventListener
    public void onShiftAssigned(ShiftAssignedEvent event) {
        Assignment assignment = event.getAssignment();
        notifications.notify(assignment.getStaff(), "Ορίστηκε αναπλήρωση",
            "Ορίστηκατε αναπληρωτής στη βάρδια " + assignment.getShift().getLabel() + ".");
    }

    @EventListener
    public void onShiftsUncovered(ShiftsUncoveredEvent event) {
        notifications.notifySupervisors("Χρειάζεται αναπλήρωση",
            event.getAbsent().getFullName() + " δήλωσε απουσία· " + event.getShifts()
            + " βάρδιες έμειναν ακάλυπτες. Δείτε τις Αναπληρώσεις.");
    }

    @EventListener
    public void onExam(ExamEvent event) {
        boolean ready = event.getType() == ExamEvent.Type.RESULT_READY;
        notifications.notify(event.getOrder().getPatient(), ready ? "Αποτέλεσμα εξέτασης" : "Παραγγελία εξέτασης",
            (ready ? "Βγήκε το αποτέλεσμα της εξέτασης «" : "Ο ιατρός σας παρήγγειλε την εξέταση «")
            + event.getOrder().getExam().getName() + "». Δείτε τα Αποτελέσματα εξετάσεων.");
    }

    private static String subjectOf(AppointmentEvent.Type type) {
        switch (type) {
            case BOOKED:
                return "Επιβεβαίωση ραντεβού";
            case RESCHEDULED:
                return "Αλλαγή ραντεβού";
            default:
                return "Ακύρωση ραντεβού";
        }
    }

    private static String textOf(AppointmentEvent.Type type) {
        switch (type) {
            case BOOKED:
                return "Το ραντεβού σας κλείστηκε για";
            case RESCHEDULED:
                return "Το ραντεβού σας μεταφέρθηκε για";
            default:
                return "Ακυρώθηκε το ραντεβού σας της";
        }
    }
}
