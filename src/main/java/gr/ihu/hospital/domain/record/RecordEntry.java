package gr.ihu.hospital.domain.record;

import java.time.LocalDateTime;

import gr.ihu.hospital.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Μία εγγραφή του ιστορικού: ποιος, τι είδους, πότε. */
@Entity
public class RecordEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private MedicalRecord record;

    @ManyToOne(optional = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type")
    private EntryType type;

    private String text;

    private LocalDateTime createdAt;

    protected RecordEntry() {
    }

    RecordEntry(MedicalRecord record, User author, EntryType type, String text, LocalDateTime createdAt) {
        this.record = record;
        this.author = author;
        this.type = type;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public MedicalRecord getRecord() {
        return record;
    }

    public User getAuthor() {
        return author;
    }

    public EntryType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
