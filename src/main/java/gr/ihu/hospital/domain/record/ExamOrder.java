package gr.ihu.hospital.domain.record;

import java.time.LocalDateTime;

import gr.ihu.hospital.domain.organization.MedicalService;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Patient;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Παραγγελία εξέτασης (UC-20, FR-22) και το αποτέλεσμά της (UC-21, FR-23).
 * Κανόνας 7 ημερών: η ίδια εξέταση δεν ξαναπαραγγέλνεται για τον ίδιο ασθενή μέσα σε 7 ημέρες.
 */
@Entity
public class ExamOrder {

    public static final int REPEAT_DAYS = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Patient patient;

    @ManyToOne(optional = false)
    private Doctor doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id")
    private MedicalService exam;

    private String notes;

    @Enumerated(EnumType.STRING)
    private ExamStatus status;

    private LocalDateTime orderedAt;

    private String resultText;

    private LocalDateTime resultAt;

    protected ExamOrder() {
    }

    public ExamOrder(Patient patient, Doctor doctor, MedicalService exam, String notes, LocalDateTime now) {
        this.patient = patient;
        this.doctor = doctor;
        this.exam = exam;
        this.notes = notes;
        this.status = ExamStatus.ORDERED;
        this.orderedAt = now;
    }

    /** Ίδια εξέταση, ίδιος ασθενής, μη ακυρωμένη, μέσα στις τελευταίες 7 ημέρες; */
    public boolean blocksRepeatOf(MedicalService other, LocalDateTime now) {
        return status != ExamStatus.CANCELLED && exam.getId() != null && exam.getId().equals(other.getId())
            && orderedAt.isAfter(now.minusDays(REPEAT_DAYS));
    }

    public void recordResult(String text, LocalDateTime now) {
        if (status != ExamStatus.ORDERED) {
            throw new IllegalStateException("Η εξέταση δεν περιμένει αποτέλεσμα.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Το αποτέλεσμα δεν μπορεί να είναι κενό.");
        }
        this.resultText = text.trim();
        this.resultAt = now;
        this.status = ExamStatus.COMPLETED;
    }

    public void cancel() {
        if (status != ExamStatus.ORDERED) {
            throw new IllegalStateException("Ακυρώνεται μόνο εξέταση σε αναμονή.");
        }
        status = ExamStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public MedicalService getExam() {
        return exam;
    }

    public String getNotes() {
        return notes;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public String getResultText() {
        return resultText;
    }

    public LocalDateTime getResultAt() {
        return resultAt;
    }
}
