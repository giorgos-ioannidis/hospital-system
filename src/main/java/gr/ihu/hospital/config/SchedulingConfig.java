package gr.ihu.hospital.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Ενεργοποιεί τις χρονοπρογραμματισμένες εργασίες (UC-27). Στα tests είναι κλειστές. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "hospital.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
