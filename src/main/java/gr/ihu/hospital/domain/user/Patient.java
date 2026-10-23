package gr.ihu.hospital.domain.user;

import java.time.LocalDate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Ασθενής (FR-02). Ο ΑΜΚΑ έχει 11 ψηφία και είναι μοναδικός. */
@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    private String amka;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;

    protected Patient() {
    }

    public Patient(String username, String passwordHash, String firstName, String lastName, String email,
                   String phone, String amka, Gender gender, LocalDate birthDate) {
        super(username, passwordHash, firstName, lastName, email, phone);
        if (amka == null || !amka.matches("\\d{11}")) {
            throw new IllegalArgumentException("Ο ΑΜΚΑ πρέπει να έχει ακριβώς 11 ψηφία.");
        }
        this.amka = amka;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    @Override
    public Role getRole() {
        return Role.PATIENT;
    }

    public String getAmka() {
        return amka;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
}
