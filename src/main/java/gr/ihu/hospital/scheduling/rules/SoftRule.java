package gr.ihu.hospital.scheduling.rules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.scheduling.Evaluation;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;

/** Μαλακός κανόνας: δίνει ποινή στον υποψήφιο (μικρότερη = καλύτερα) και αναφέρει ανισορροπίες. */
public abstract class SoftRule extends Rule {

    private final int weight;

    protected SoftRule(String name, int weight) {
        super(name);
        this.weight = weight;
    }

    @Override
    protected final void apply(Assignment candidate, ScheduleState state, Evaluation result) {
        result.addPenalty(weight * penalty(candidate, state));
    }

    protected abstract int penalty(Assignment candidate, ScheduleState state);

    /** Αναφέρει παραβίαση όταν, μέσα σε μια ειδικότητα, η διαφορά max − min ξεπερνά το όριο. */
    protected final void reportSpread(ScheduleState state, List<Violation> found,
                                      ToIntFunction<HealthStaff> measure, int maxSpread, String unit) {
        Map<Specialty, List<HealthStaff>> bySpecialty = state.getContext().getStaff().stream()
            .collect(Collectors.groupingBy(HealthStaff::getSpecialty, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<Specialty, List<HealthStaff>> group : bySpecialty.entrySet()) {
            int max = group.getValue().stream().mapToInt(measure).max().orElse(0);
            int min = group.getValue().stream().mapToInt(measure).min().orElse(0);
            if (max - min > maxSpread) {
                String detail = group.getKey().getName() + ": διαφορά " + (max - min) + " " + unit;
                found.add(new Violation(getName(), false, detail));
            }
        }
    }
}
