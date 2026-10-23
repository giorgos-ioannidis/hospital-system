package gr.ihu.hospital.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** UC-04 / FR-04: στοιχεία επικοινωνίας (UI-04). */
public class ContactForm {

    @NotBlank
    @Email
    private String email;

    @Size(max = 30)
    private String phone;

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
}
