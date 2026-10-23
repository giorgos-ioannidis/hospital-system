package gr.ihu.hospital.domain.schedule;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Ελάχιστη στελέχωση ενός τμήματος ανά τύπο βάρδιας και ειδικότητα (παράμετρος του UC-05). */
@Entity
public class StaffingRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Department department;

    @ManyToOne(optional = false)
    private Specialty specialty;

    @Enumerated(EnumType.STRING)
    private ShiftType shiftType;

    private int minStaff;

    protected StaffingRequirement() {
    }

    public StaffingRequirement(Department department, Specialty specialty, ShiftType shiftType, int minStaff) {
        this.department = department;
        this.specialty = specialty;
        this.shiftType = shiftType;
        this.minStaff = minStaff;
    }

    public Department getDepartment() {
        return department;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public int getMinStaff() {
        return minStaff;
    }
}
