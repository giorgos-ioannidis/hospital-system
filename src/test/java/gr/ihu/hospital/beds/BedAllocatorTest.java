package gr.ihu.hospital.beds;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import gr.ihu.hospital.domain.organization.Admission;
import gr.ihu.hospital.domain.organization.Bed;
import gr.ihu.hospital.domain.organization.BedRequest;
import gr.ihu.hospital.domain.organization.Department;
import gr.ihu.hospital.domain.organization.Ward;
import gr.ihu.hospital.domain.user.Gender;
import gr.ihu.hospital.domain.user.Patient;

/** UC-17 / FR-18: κατανομή κλινών με φύλο, απομόνωση και βαρύτητα. */
class BedAllocatorTest {

    private static final Department CARDIOLOGY = new Department("Καρδιολογικό", 1);
    private static final Department SURGERY = new Department("Χειρουργικό", 3);
    private final Ward nearStation = new Ward(CARDIOLOGY, "Κ-Α", true);
    private final Ward farWard = new Ward(CARDIOLOGY, "Κ-Β", false);
    private final Ward single = new Ward(CARDIOLOGY, "Κ-Ι", true);
    private final Bed a1 = new Bed(nearStation, "Α1", false);
    private final Bed a2 = new Bed(nearStation, "Α2", false);
    private final Bed b1 = new Bed(farWard, "Β1", false);
    private final Bed b2 = new Bed(farWard, "Β2", false);
    private final Bed isolation = new Bed(single, "Ι1", true);
    private final List<Bed> beds = Arrays.asList(a1, a2, b1, b2, isolation);
    private final BedAllocator allocator = new BedAllocator();

    @Test
    void bedAllocator_neverMixesGendersInAWard() {
        List<Admission> active = Collections.singletonList(admit(patient(Gender.M), a1));

        List<Bed> forWoman = allocator.suggest(new BedRequest(Gender.F, 2, false), beds, Collections.<Bed>emptyList(), active);

        assertThat(forWoman).doesNotContain(a1, a2).contains(b1, b2);
    }

    @Test
    void bedAllocator_isolationOnlyInIsolationBedsAndSevereNearStation() {
        assertThat(allocator.suggest(new BedRequest(Gender.M, 2, true), beds, Collections.<Bed>emptyList(),
                                     Collections.<Admission>emptyList())).containsExactly(isolation);
        assertThat(allocator.suggest(new BedRequest(Gender.M, 3, false), beds, Collections.<Bed>emptyList(),
                                     Collections.<Admission>emptyList()))
            .as("βαρύτητα 3: πρώτα οι κλίνες κοντά στον σταθμό, η απομόνωση τελευταία").containsExactly(a1, a2, b1);
        assertThat(allocator.suggest(new BedRequest(Gender.M, 1, false), beds, Collections.<Bed>emptyList(),
                                     Collections.<Admission>emptyList()).get(0))
            .as("ελαφρύ περιστατικό: μακριά από τον σταθμό").isEqualTo(b1);
    }

    @Test
    void bedAllocator_looksInOtherDepartmentsWhenFull() {
        List<Admission> full = new ArrayList<>();
        for (Bed bed : beds) {
            full.add(admit(patient(Gender.M), bed));
        }
        Bed surgeryBed = new Bed(new Ward(SURGERY, "Χ-Α", true), "Α1", false);

        assertThat(allocator.suggest(new BedRequest(Gender.M, 2, false), beds, Collections.singletonList(surgeryBed), full))
            .containsExactly(surgeryBed);
    }

    private static Patient patient(Gender gender) {
        return new Patient("p" + gender, "h", "Α", "Β", "p@test", null, "01019000001", gender, LocalDate.of(1980, 1, 1));
    }

    private static Admission admit(Patient patient, Bed bed) {
        return new Admission(patient, bed, new BedRequest(patient.getGender(), 2, bed.isIsolation()), null, LocalDateTime.now());
    }
}
