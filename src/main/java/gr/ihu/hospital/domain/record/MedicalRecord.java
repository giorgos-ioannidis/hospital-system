package gr.ihu.hospital.domain.record;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;

/** Ηλεκτρονικός ιατρικός φάκελος ενός ασθενούς (UC-19). Οι εγγραφές μόνο προστίθενται. */
@Entity
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private Patient patient;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC, id DESC")
    private List<RecordEntry> entries = new ArrayList<>();

    protected MedicalRecord() {
    }

    public MedicalRecord(Patient patient, LocalDateTime now) {
        this.patient = patient;
        this.createdAt = now;
    }

    public RecordEntry addEntry(User author, EntryType type, String text, LocalDateTime now) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Η εγγραφή δεν μπορεί να είναι κενή.");
        }
        RecordEntry entry = new RecordEntry(this, author, type, text.trim(), now);
        entries.add(0, entry);
        return entry;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<RecordEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
