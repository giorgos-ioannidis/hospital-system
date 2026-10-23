package gr.ihu.hospital.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** UC-04 / FR-04: αλλαγή κωδικού (UI-04). */
public class PasswordForm {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 72, message = "Τουλάχιστον 8 χαρακτήρες.")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
