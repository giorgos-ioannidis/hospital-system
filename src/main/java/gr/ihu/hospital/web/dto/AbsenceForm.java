package gr.ihu.hospital.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** UC-09 / FR-09: δήλωση απουσίας (UI-08). */
public class AbsenceForm {

    @NotNull
    private LocalDate startDate;

    @NotNull
    @FutureOrPresent(message = "Η λήξη δεν μπορεί να είναι στο παρελθόν.")
    private LocalDate endDate;

    @Size(max = 500)
    private String reason;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
