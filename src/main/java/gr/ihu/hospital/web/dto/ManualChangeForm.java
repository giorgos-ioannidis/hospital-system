package gr.ihu.hospital.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** UC-05: χειροκίνητη αλλαγή ανάθεσης (UI-05b). Το slot είναι «specialtyId:SHIFT_TYPE». */
public class ManualChangeForm {

    @NotNull
    private LocalDate date;

    @NotBlank
    private String slot;

    @NotNull
    private Long staffId;

    private boolean add = true;

    private boolean confirm;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(boolean add) {
        this.add = add;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }
}
