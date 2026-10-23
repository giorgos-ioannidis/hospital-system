package gr.ihu.hospital.service;

import java.util.List;

import gr.ihu.hospital.domain.organization.Admission;
import gr.ihu.hospital.domain.record.EntryType;
import gr.ihu.hospital.domain.record.ExamOrder;
import gr.ihu.hospital.domain.record.RecordEntry;
import gr.ihu.hospital.domain.user.Patient;

/** Ό,τι δείχνει το UI-18b: ο φάκελος, οι εξετάσεις, οι νοσηλείες και τα είδη εγγραφών που επιτρέπεται να γράψει ο χρήστης. */
public final class RecordView {

    private final Patient patient;
    private final List<RecordEntry> entries;
    private final List<ExamOrder> exams;
    private final List<Admission> admissions;
    private final List<EntryType> writableTypes;

    public RecordView(Patient patient, List<RecordEntry> entries, List<ExamOrder> exams, List<Admission> admissions,
                      List<EntryType> writableTypes) {
        this.patient = patient;
        this.entries = entries;
        this.exams = exams;
        this.admissions = admissions;
        this.writableTypes = writableTypes;
    }

    public Patient getPatient() {
        return patient;
    }

    public List<RecordEntry> getEntries() {
        return entries;
    }

    public List<ExamOrder> getExams() {
        return exams;
    }

    public List<Admission> getAdmissions() {
        return admissions;
    }

    public List<EntryType> getWritableTypes() {
        return writableTypes;
    }

    public boolean isWritable() {
        return !writableTypes.isEmpty();
    }
}
