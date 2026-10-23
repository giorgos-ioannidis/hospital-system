package gr.ihu.hospital.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.record.AccessAction;
import gr.ihu.hospital.domain.record.ExamOrder;
import gr.ihu.hospital.domain.record.RecordEntry;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.pdf.PdfExporter;

/** UC-26 Εξαγωγή PDF: «extend» των UC-08 (πρόγραμμα), UC-15 (ραντεβού), UC-19 (ιστορικό) και του UC-21 (αποτέλεσμα). */
@Service
@Transactional
public class ExportService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE dd/MM/yyyy");
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PdfExporter pdf;
    private final ScheduleService schedules;
    private final AppointmentManagementService appointments;
    private final MedicalRecordService records;
    private final ExamService exams;

    public ExportService(PdfExporter pdf, ScheduleService schedules, AppointmentManagementService appointments,
                         MedicalRecordService records, ExamService exams) {
        this.pdf = pdf;
        this.schedules = schedules;
        this.appointments = appointments;
        this.records = records;
        this.exams = exams;
    }

    /** FR-29: το προσωπικό πρόγραμμα βαρδιών του μήνα. */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public byte[] mySchedule(String username, YearMonth month) {
        List<List<String>> rows = new ArrayList<>();
        for (Assignment a : schedules.mySchedule(username, month)) {
            rows.add(Arrays.asList(a.getShift().getDate().format(DAY), a.getShift().getType().getLabel(),
                a.getShift().getStart().format(WHEN) + " – " + a.getShift().getEnd().format(WHEN),
                a.getShift().getSchedule().getDepartment().getName()));
        }
        return pdf.export("Πρόγραμμα βαρδιών " + month, Collections.singletonList("Χρήστης: " + username),
                          Arrays.asList("Ημέρα", "Βάρδια", "Διάστημα", "Τμήμα"), rows);
    }

    /** FR-29: τα ραντεβού του ασθενούς. */
    @PreAuthorize("hasRole('PATIENT')")
    public byte[] myAppointments(String username) {
        List<List<String>> rows = new ArrayList<>();
        for (Appointment a : appointments.mine(username)) {
            rows.add(Arrays.asList(a.getCode(), a.getStart().format(WHEN), a.getDoctor().getFullName(),
                a.getService().getName(), a.getRoom().getName(), a.getStatus().getLabel()));
        }
        return pdf.export("Τα ραντεβού μου", Collections.singletonList("Χρήστης: " + username),
                          Arrays.asList("Κωδικός", "Ημέρα και ώρα", "Ιατρός", "Υπηρεσία", "Αίθουσα", "Κατάσταση"), rows);
    }

    /** FR-30: το ιατρικό ιστορικό, μόνο για όσους έχουν πρόσβαση (καταγράφεται ως εξαγωγή). */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PATIENT')")
    public byte[] record(String username, Long patientId) {
        Patient patient = records.requireView(username, patientId, AccessAction.EXPORT_PDF);
        List<List<String>> rows = new ArrayList<>();
        for (RecordEntry e : records.entriesOf(patient)) {
            rows.add(Arrays.asList(e.getCreatedAt().format(WHEN), e.getType().getLabel(), e.getText(),
                                   e.getAuthor().getFullName()));
        }
        return pdf.export("Ιατρικό ιστορικό", patientLines(patient),
                          Arrays.asList("Ημερομηνία", "Είδος", "Εγγραφή", "Συντάκτης"), rows);
    }

    /** FR-23: λήψη αποτελέσματος εξέτασης. */
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PATIENT')")
    public byte[] examResult(String username, Long orderId) {
        ExamOrder order = exams.result(username, orderId);
        List<String> details = new ArrayList<>(patientLines(order.getPatient()));
        details.add("Παραγγελία: " + order.getOrderedAt().format(WHEN) + " από " + order.getDoctor().getFullName());
        List<List<String>> rows = Collections.singletonList(Arrays.asList(order.getExam().getName(),
            order.getStatus().getLabel(), order.getResultText() == null ? "Σε αναμονή" : order.getResultText()));
        return pdf.export("Αποτέλεσμα εξέτασης", details, Arrays.asList("Εξέταση", "Κατάσταση", "Αποτέλεσμα"), rows);
    }

    private static List<String> patientLines(Patient patient) {
        return Arrays.asList("Ασθενής: " + patient.getFullName(), "ΑΜΚΑ: " + patient.getAmka());
    }
}
