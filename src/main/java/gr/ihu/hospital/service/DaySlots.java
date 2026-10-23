package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Οι ώρες μιας ημέρας για το πλέγμα επιλογής ώρας (UI-11, UI-14b): ελεύθερες και κλεισμένες. */
public final class DaySlots {

    private final LocalDate date;
    private final List<Option> options = new ArrayList<>();

    DaySlots(LocalDate date) {
        this.date = date;
    }

    void add(LocalDateTime start, boolean free) {
        options.add(new Option(start, free));
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public long getFreeCount() {
        return options.stream().filter(Option::isFree).count();
    }

    /** Μία ώρα του πλέγματος. */
    public static final class Option {

        private final LocalDateTime start;
        private final boolean free;

        Option(LocalDateTime start, boolean free) {
            this.start = start;
            this.free = free;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public boolean isFree() {
            return free;
        }
    }
}
