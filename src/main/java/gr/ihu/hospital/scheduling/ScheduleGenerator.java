package gr.ihu.hospital.scheduling;

/** Strategy (FR-05): παράγει πρόχειρο (DRAFT) μηνιαίο πρόγραμμα για το τμήμα του context. */
public interface ScheduleGenerator {

    ScheduleResult generate(SchedulingContext context);
}
