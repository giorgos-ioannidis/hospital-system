package gr.ihu.hospital.domain.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.user.Supervisor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

/** Μηνιαίο πρόγραμμα ενός τμήματος (UC-05). Σώζεται ως DRAFT και γίνεται ACTIVE με έγκριση. */
@Entity
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Department department;

    private LocalDate periodStart;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Supervisor approvedBy;

    private int maxWeeklyHours;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC, id ASC")
    private List<Shift> shifts = new ArrayList<>();

    protected Schedule() {
    }

    public Schedule(Department department, YearMonth month, int maxWeeklyHours) {
        this.department = department;
        this.periodStart = month.atDay(1);
        this.maxWeeklyHours = maxWeeklyHours;
        this.status = ScheduleStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public Shift addShift(Specialty specialty, LocalDate date, ShiftType type, int minStaff) {
        if (!YearMonth.from(date).equals(getMonth())) {
            throw new IllegalArgumentException("Η βάρδια " + date + " δεν ανήκει στον μήνα του προγράμματος.");
        }
        Shift shift = new Shift(this, specialty, date, type, minStaff);
        shifts.add(shift);
        return shift;
    }

    /** Ο Προϊστάμενος εγκρίνει το πρόχειρο: DRAFT → ACTIVE. */
    public void approve(Supervisor supervisor) {
        if (status != ScheduleStatus.DRAFT) {
            throw new IllegalStateException("Εγκρίνεται μόνο πρόχειρο (DRAFT) πρόγραμμα.");
        }
        status = ScheduleStatus.ACTIVE;
        approvedBy = supervisor;
    }

    public void archive() {
        status = ScheduleStatus.ARCHIVED;
    }

    /** Οι βάρδιες κάτω από την ελάχιστη στελέχωση (UI-05b σε κόκκινο). */
    public List<Shift> getUncoveredShifts() {
        return shifts.stream().filter(Shift::isUnderstaffed).collect(Collectors.toList());
    }

    public YearMonth getMonth() {
        return YearMonth.from(periodStart);
    }

    public boolean isDraft() {
        return status == ScheduleStatus.DRAFT;
    }

    public Long getId() {
        return id;
    }

    public Department getDepartment() {
        return department;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Supervisor getApprovedBy() {
        return approvedBy;
    }

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public List<Shift> getShifts() {
        return Collections.unmodifiableList(shifts);
    }
}
