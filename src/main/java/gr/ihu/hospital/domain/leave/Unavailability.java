package gr.ihu.hospital.domain.leave;

import java.time.LocalDateTime;

import gr.ihu.hospital.domain.user.HealthStaff;

/** Κάτι που κάνει έναν εργαζόμενο μη διαθέσιμο σε ένα διάστημα [from, to): άδεια, απουσία, ώρες υποδοχής. */
public interface Unavailability {

    boolean blocks(HealthStaff staff, LocalDateTime from, LocalDateTime to);
}
