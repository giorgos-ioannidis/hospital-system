package gr.ihu.hospital.domain.appointment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Χρονικό διάστημα [start, end). Δύο διαστήματα που απλώς ακουμπούν δεν επικαλύπτονται. */
public final class TimeSlot {

    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("EEE dd/MM HH:mm");

    private final LocalDateTime start;
    private final LocalDateTime end;

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Το τέλος πρέπει να είναι μετά την αρχή.");
        }
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    public boolean overlaps(TimeSlot other) {
        return overlaps(other.start, other.end);
    }

    public boolean contains(TimeSlot other) {
        return !other.start.isBefore(start) && !other.end.isAfter(end);
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getLabel() {
        return start.format(LABEL);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TimeSlot && start.equals(((TimeSlot) other).start) && end.equals(((TimeSlot) other).end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
