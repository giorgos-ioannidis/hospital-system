package gr.ihu.hospital.domain.record;

import gr.ihu.hospital.domain.user.Doctor;
import gr.ihu.hospital.domain.user.Nurse;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;

/**
 * NFR-02 / FR-20 / FR-21: ποιος βλέπει και ποιος γράφει σε έναν φάκελο.
 * Ο ασθενής βλέπει μόνο τον δικό του και γράφει μόνο σημειώσεις ασθενούς. Ιατρός και νοσηλευτής βλέπουν μόνο αν
 * «θεραπεύουν» τον ασθενή (νοσηλεία στο τμήμα τους ή ραντεβού/εξέταση με τον ιατρό). Μόνο ο ιατρός γράφει κλινικά.
 * Ο Προϊστάμενος δεν έχει πρόσβαση σε κλινικά δεδομένα.
 */
public final class RecordAccessPolicy {

    public boolean canView(User viewer, Patient patient, boolean treating) {
        if (viewer instanceof Patient) {
            return sameUser(viewer, patient);
        }
        return (viewer instanceof Doctor || viewer instanceof Nurse) && treating;
    }

    public boolean canWrite(User author, Patient patient, boolean treating, EntryType type) {
        if (author instanceof Patient) {
            return sameUser(author, patient) && !type.isClinical();
        }
        return author instanceof Doctor && treating && type.isClinical();
    }

    private static boolean sameUser(User a, User b) {
        return a == b || a.getId() != null && a.getId().equals(b.getId());
    }
}
