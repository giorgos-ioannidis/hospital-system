package gr.ihu.hospital.record;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.record.EntryType;
import gr.ihu.hospital.domain.record.RecordAccessPolicy;
import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Gender;
import gr.ihu.hospital.domain.user.Nurse;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.StaffAccountData;
import gr.ihu.hospital.domain.user.Supervisor;

/** NFR-02, FR-20, FR-21: ποιος βλέπει και ποιος γράφει στον φάκελο. */
class RecordAccessPolicyTest {

    private final RecordAccessPolicy policy = new RecordAccessPolicy();
    private final Patient anna = patient("anna");
    private final Patient other = patient("other");
    private final Doctor doctor = new Doctor(staff("doc"));
    private final Nurse nurse = new Nurse(staff("nurse"));

    @Test
    void patientSeesOnlyOwnRecordAndWritesOnlyPatientNotes() {
        assertThat(policy.canView(anna, anna, false)).isTrue();
        assertThat(policy.canView(anna, other, true)).isFalse();
        assertThat(policy.canWrite(anna, anna, false, EntryType.PATIENT_NOTE)).isTrue();
        assertThat(policy.canWrite(anna, anna, false, EntryType.DIAGNOSIS)).isFalse();
        assertThat(policy.canWrite(anna, other, false, EntryType.PATIENT_NOTE)).isFalse();
    }

    @Test
    void staffNeedsTreatingRelationAndOnlyDoctorsWriteClinically() {
        assertThat(policy.canView(doctor, anna, false)).isFalse();
        assertThat(policy.canView(nurse, anna, true)).isTrue();
        assertThat(policy.canWrite(doctor, anna, true, EntryType.DIAGNOSIS)).isTrue();
        assertThat(policy.canWrite(doctor, anna, true, EntryType.PATIENT_NOTE)).isFalse();
        assertThat(policy.canWrite(nurse, anna, true, EntryType.NOTE)).isFalse();
        assertThat(policy.canView(new Supervisor(staff("sup")), anna, true)).isFalse();
    }

    private static Patient patient(String name) {
        return new Patient(name, "h", "Α", name, name + "@t", null, "01019000001", Gender.F, LocalDate.of(1990, 1, 1));
    }

    private static StaffAccountData staff(String name) {
        return new StaffAccountData(name, "h", "Α", name, name + "@t", null, new Department("Κ", 1), new Specialty("Σ"));
    }
}
