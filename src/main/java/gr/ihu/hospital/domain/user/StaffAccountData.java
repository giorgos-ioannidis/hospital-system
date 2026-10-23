package gr.ihu.hospital.domain.user;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;

/** Τα στοιχεία ενός νέου λογαριασμού προσωπικού (είσοδος του UserFactory). */
public final class StaffAccountData {

    private final String username;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final Department department;
    private final Specialty specialty;

    public StaffAccountData(String username, String passwordHash, String firstName, String lastName,
                            String email, String phone, Department department, Specialty specialty) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.specialty = specialty;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Department getDepartment() {
        return department;
    }

    public Specialty getSpecialty() {
        return specialty;
    }
}
