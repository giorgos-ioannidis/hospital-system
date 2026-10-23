package gr.ihu.hospital.scheduling.rules;

import java.util.List;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.scheduling.Evaluation;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;

/**
 * Κανόνας προγράμματος. Chain of Responsibility: κάθε κανόνας ελέγχει και περνά στον επόμενο.
 * Template Method: τα evaluate/review είναι σταθερά, οι υποκλάσεις υλοποιούν apply/inspect.
 */
public abstract class Rule {

    private final String name;
    private Rule next;

    protected Rule(String name) {
        this.name = name;
    }

    void setNext(Rule next) {
        this.next = next;
    }

    /** Ελέγχει μια υποψήφια ανάθεση με όλους τους κανόνες της αλυσίδας. */
    public final void evaluate(Assignment candidate, ScheduleState state, Evaluation result) {
        apply(candidate, state, result);
        if (next != null) {
            next.evaluate(candidate, state, result);
        }
    }

    /** Ελέγχει ολόκληρο το πρόγραμμα (π.χ. ακάλυπτες βάρδιες, άνιση κατανομή). */
    public final void review(ScheduleState state, List<Violation> found) {
        inspect(state, found);
        if (next != null) {
            next.review(state, found);
        }
    }

    protected abstract void apply(Assignment candidate, ScheduleState state, Evaluation result);

    /** Προεπιλογή: ο κανόνας δεν έχει έλεγχο σε επίπεδο προγράμματος. */
    protected void inspect(ScheduleState state, List<Violation> found) {
    }

    public String getName() {
        return name;
    }
}
