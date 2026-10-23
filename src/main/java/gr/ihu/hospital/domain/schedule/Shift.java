package gr.ihu.hospital.domain.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.user.HealthStaff;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/** Μία βάρδια μιας ημέρας για μία ειδικότητα, με την ελάχιστη στελέχωσή της και τις αναθέσεις. */
@Entity
public class Shift {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Schedule schedule;

    @ManyToOne(optional = false)
    private Specialty specialty;

    @Column(name = "shift_date")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type")
    private ShiftType type;

    private int minStaff;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    protected Shift() {
    }

    Shift(Schedule schedule, Specialty specialty, LocalDate date, ShiftType type, int minStaff) {
        this.schedule = schedule;
        this.specialty = specialty;
        this.date = date;
        this.type = type;
        this.minStaff = minStaff;
    }

    public void addAssignment(Assignment assignment) {
        if (assignment.getShift() != this) {
            throw new IllegalArgumentException("Η ανάθεση αφορά άλλη βάρδια.");
        }
        assignments.add(assignment);
    }

    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
    }

    public boolean isAssigned(HealthStaff staff) {
        return findAssignment(staff) != null;
    }

    /** Η ανάθεση του εργαζομένου σε αυτή τη βάρδια, ή null. */
    public Assignment findAssignment(HealthStaff staff) {
        for (Assignment assignment : assignments) {
            if (assignment.getStaff() == staff) {
                return assignment;
            }
        }
        return null;
    }

    public boolean isUnderstaffed() {
        return assignments.size() < minStaff;
    }

    public int getMissing() {
        return Math.max(0, minStaff - assignments.size());
    }

    public boolean isWeekend() {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public LocalDateTime getStart() {
        return date.atTime(type.getStart());
    }

    public LocalDateTime getEnd() {
        return getStart().plusHours(type.getHours());
    }

    public int getHours() {
        return type.getHours();
    }

    /** π.χ. «05/11 Πρωί (Νοσηλευτική)». */
    public String getLabel() {
        return date.format(DAY) + " " + type.getLabel() + " (" + specialty.getName() + ")";
    }

    public Long getId() {
        return id;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public LocalDate getDate() {
        return date;
    }

    public ShiftType getType() {
        return type;
    }

    public int getMinStaff() {
        return minStaff;
    }

    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }
}
