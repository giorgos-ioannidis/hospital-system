package gr.ihu.hospital.web.dto;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.domain.user.Supervisor;
import gr.ihu.hospital.domain.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** UC-03 / FR-03: επεξεργασία λογαριασμού προσωπικού (UI-03b). */
public class StaffEditForm {

    @NotBlank
    @Email
    private String email;

    @Size(max = 30)
    private String phone;

    @NotNull
    private Long departmentId;

    private Long specialtyId;

    /** Η φόρμα γεμάτη με τα τρέχοντα στοιχεία του λογαριασμού. */
    public static StaffEditForm of(User user) {
        StaffEditForm form = new StaffEditForm();
        form.email = user.getEmail();
        form.phone = user.getPhone();
        if (user instanceof HealthStaff) {
            form.departmentId = ((HealthStaff) user).getDepartment().getId();
            form.specialtyId = ((HealthStaff) user).getSpecialty().getId();
        } else {
            Department department = ((Supervisor) user).getDepartment();
            form.departmentId = department == null ? null : department.getId();
        }
        return form;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }
}
