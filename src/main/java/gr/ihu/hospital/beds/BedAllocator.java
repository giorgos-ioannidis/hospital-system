package gr.ihu.hospital.beds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gr.ihu.hospital.domain.organization.Admission;
import gr.ihu.hospital.domain.organization.Bed;
import gr.ihu.hospital.domain.organization.BedRequest;

/**
 * UC-17 / FR-18 (§7.6): προτείνει κλίνη για εισαγωγή.
 * Σκληροί κανόνες: ελεύθερη κλίνη, απομόνωση → μόνο κλίνη απομόνωσης, φύλο θαλάμου (δεν μπαίνει ασθενής σε θάλαμο
 * με νοσηλευόμενους άλλου φύλου). Μαλακοί: βαρύτητα 3 κοντά στον σταθμό νοσηλευτών, οι κλίνες απομόνωσης και οι θέσεις
 * κοντά στον σταθμό φυλάσσονται για όσους τις χρειάζονται. Επιστρέφει την καλύτερη και 2 εναλλακτικές· αν δεν υπάρχει
 * καμία στο τμήμα, ψάχνει στα άλλα τμήματα.
 */
public final class BedAllocator {

    public static final int SUGGESTIONS = 3;
    static final int SEVERE_FAR_FROM_STATION = 10;
    static final int ISOLATION_BED_WASTED = 20;
    static final int STATION_BED_FOR_LIGHT_CASE = 2;

    public List<Bed> suggest(BedRequest request, List<Bed> departmentBeds, List<Bed> otherBeds,
                             List<Admission> active) {
        List<Bed> found = rank(request, departmentBeds, active);
        return found.isEmpty() ? rank(request, otherBeds, active) : found;
    }

    /** Οι σκληροί κανόνες· ξαναελέγχονται και τη στιγμή της εισαγωγής. */
    public boolean fits(BedRequest request, Bed bed, List<Admission> active) {
        if (isOccupied(bed, active) || request.isIsolation() && !bed.isIsolation()) {
            return false;
        }
        return bed.isIsolation() || !hasOtherGender(request, bed, active);
    }

    int penalty(BedRequest request, Bed bed) {
        int penalty = 0;
        boolean nearStation = bed.getWard().isNearStation();
        if (request.getSeverity() == BedRequest.MAX_SEVERITY && !nearStation) {
            penalty += SEVERE_FAR_FROM_STATION;
        }
        if (!request.isIsolation() && bed.isIsolation()) {
            penalty += ISOLATION_BED_WASTED;
        }
        if (request.getSeverity() == 1 && nearStation) {
            penalty += STATION_BED_FOR_LIGHT_CASE;
        }
        return penalty;
    }

    private List<Bed> rank(BedRequest request, List<Bed> beds, List<Admission> active) {
        List<Bed> fitting = new ArrayList<>();
        for (Bed bed : beds) {
            if (fits(request, bed, active)) {
                fitting.add(bed);
            }
        }
        fitting.sort(Comparator.comparingInt((Bed bed) -> penalty(request, bed)).thenComparing(Bed::getName));
        return new ArrayList<>(fitting.subList(0, Math.min(SUGGESTIONS, fitting.size())));
    }

    private static boolean isOccupied(Bed bed, List<Admission> active) {
        for (Admission admission : active) {
            if (admission.isActive() && admission.getBed() == bed) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOtherGender(BedRequest request, Bed bed, List<Admission> active) {
        for (Admission admission : active) {
            boolean sameWard = admission.getBed().getWard() == bed.getWard();
            if (admission.isActive() && sameWard && admission.getPatient().getGender() != request.getGender()) {
                return true;
            }
        }
        return false;
    }
}
