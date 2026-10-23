package gr.ihu.hospital.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gr.ihu.hospital.domain.appointment.OfficeHours;

/** Αποτέλεσμα του UC-11 με επανάληψη: ποιες ημέρες καταχωρήθηκαν και ποιες απορρίφθηκαν (και γιατί). */
public final class OfficeHoursResult {

    private final List<OfficeHours> created = new ArrayList<>();
    private final Map<LocalDate, String> rejected = new LinkedHashMap<>();

    void created(OfficeHours hours) {
        created.add(hours);
    }

    void rejected(LocalDate date, String reason) {
        rejected.put(date, reason);
    }

    public List<OfficeHours> getCreated() {
        return Collections.unmodifiableList(created);
    }

    public Map<LocalDate, String> getRejected() {
        return Collections.unmodifiableMap(rejected);
    }
}
