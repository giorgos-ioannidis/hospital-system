# Μετρικές ποιότητας (NFR-07)

Επίσημη μέτρηση με το **MetricsCalculator** του μαθήματος (Moodle), στο `official/metrics.csv` (μία γραμμή ανά
αρχείο, διαχωριστικό `;`). Δευτερεύουσα μέτρηση με το **CK 0.7.0** στο `ck/` (ανά κλάση και ανά μέθοδο), που δίνει
και το CC κάθε μεθόδου ξεχωριστά. Το `ck/` είναι από την προηγούμενη μέτρηση και δεν περιέχει το `SupervisorScope`.

## Πώς τρέχει
Το MetricsCalculator είναι εφαρμογή Swing (Java 8). Με διπλό κλικ στο macOS συχνά **δεν ανοίγει**, επειδή το αρχείο
έχει σήμανση καραντίνας από τον browser. Δύο τρόποι:

```bash
# Γραμμή εντολών (χωρίς παράθυρο): <φάκελος κώδικα> <αρχείο εξόδου CSV ή "str">
java -jar ~/Downloads/MetricsCalculator.jar "$PWD/src/main/java" "$PWD/docs/metrics/official/metrics.csv"

# Γραφικό περιβάλλον: αφαίρεση της καραντίνας και εκκίνηση χωρίς ορίσματα
xattr -d com.apple.quarantine ~/Downloads/MetricsCalculator.jar
java -jar ~/Downloads/MetricsCalculator.jar
```
Το μήνυμα `NullPointerException ... superClass is null` είναι αβλαβές: εμφανίζεται για κλάσεις που κληρονομούν από
κλάση βιβλιοθήκης (π.χ. `RecordAccessDeniedException extends AccessDeniedException`) και επηρεάζει μόνο τη στήλη MFA.

## Τι σημαίνουν οι στήλες (όπως τις υπολογίζει το εργαλείο)
- **WMC**: πλήθος μεθόδων (κάθε μέθοδος μετρά 1). Το ίδιο με το NOM.
- **WMC\***: μέση κυκλωματική πολυπλοκότητα (CC) ανά μέθοδο.
- **LCOM**: LCOM1, δηλαδή ζεύγη μεθόδων χωρίς κοινό πεδίο.
- **SIZE1**: γραμμές κώδικα. **SIZE2**: ιδιότητες και μέθοδοι.
- **DIT, NOCC, CBO, RFC, FanIn**: όπως στο μάθημα.
- Οι υπόλοιπες στήλες είναι μετρικές QMOOD.

## Σύνοψη (Επαύξηση 3, μετά το UC-03 `SupervisorScope`)

| Μετρική | Τιμή | Στόχος NFR-07 | |
|---|---|---|---|
| Αρχεία / μέθοδοι | 173 / 732 | — | |
| LoC (SIZE1, άθροισμα) | 3.561 | — | |
| CC ανά μέθοδο | μέσος όρος ανά κλάση ≤ 5 (`UserFactory`)· μέγιστο σε μία μέθοδο = 8 (CK) | ≤ 10 | ✅ |
| WMPC1 ανά κλάση | MetricsCalculator: WMC ≤ 23 (`Appointment`)· CK (άθροισμα CC): ≤ 30 | ≤ 30 | ✅ |
| DIT, μέγιστο | 2 (κανόνες: `Rule` → `HardRule` → `SpecialtyRule`) | — | |
| NOCC, μέγιστο | 6 (`HardRule`), 3 (`User`) | — | |
| CBO, μέγιστο | 25 (`AppointmentService`) | — | |
| RFC, μέγιστο | 110 (`AppointmentService`) | — | |
| FanIn, μέγιστο | 31 (`HealthStaff`) | — | |
| LCOM1 = 0 | domain 19/51, scheduling 6/24, booking 0/4, service 3/23 | = 0 στο πεδίο | ❌ (βλ. παρακάτω) |

## Τι διορθώθηκε μετά την 1η μέτρηση (CK)
Έξι κλάσεις είχαν άθροισμα CC (WMPC1) > 30. Χωρίστηκαν εκεί όπου έκαναν δύο δουλειές:

| Πριν | Μετά |
|---|---|
| `ScheduleService` (37) | `ScheduleService` (UC-05/08) + `ScheduleChangeService` (χειροκίνητη αλλαγή) |
| `AppointmentService` (34) | `AppointmentService` (UC-13/14/16) + `AppointmentManagementService` (UC-15/28) |
| `AccountService` (33) | `AccountService` (UC-02/04) + `StaffAccountService` (UC-03) |
| `AvailabilityChecker` (36) | `AvailabilityChecker` + `ResourceBookings` (κρατήσεις ιατρού/αίθουσας/εξοπλισμού) |
| `Appointment` (36) | οι κανόνες μετάβασης κατάστασης μεταφέρθηκαν στο `AppointmentStatus` |
| `ScheduleState` (31) | το backtracking (`recentAssignments`) μεταφέρθηκε στον `GreedyScheduleGenerator` |

## LCOM1 = 0: πρόταση προς την ομάδα
Το LCOM1 μετρά ζεύγη μεθόδων που δεν μοιράζονται κανένα πεδίο. Γι' αυτό:
- **Οντότητες JPA** (π.χ. `Appointment` 253 = όλα τα ζεύγη των 23 μεθόδων): κάθε getter διαβάζει άλλο πεδίο, άρα το
  LCOM1 είναι αναπόφευκτα > 0. Το είχε προβλέψει και το master prompt (§2).
- **Κλάσεις λογικής**: είναι συνεκτικές ως προς τον σκοπό τους, αλλά οι βοηθητικές μέθοδοι διαβάζουν διαφορετικά πεδία
  (π.χ. ο `AvailabilityChecker` ελέγχει χωριστά βάρδιες, άδειες και ώρες υποδοχής).
- Με LCOM1 = 0 βγαίνουν οι κλάσεις με μία ευθύνη: 4 από τους 8 κανόνες (`SpecialtyRule`, `ApprovedLeaveRule`,
  `RestAfterOnCallRule`, `MaxWeeklyHoursRule`), οι πύλες πληρωμής και οι ρυθμίσεις. Οι άλλοι 4 κανόνες έχουν LCOM1 = 1,
  επειδή έχουν μια βοηθητική μέθοδο ή σταθερά που δεν τη χρησιμοποιεί η δεύτερη μέθοδος.

Προτείνουμε το NFR-07 να γίνει: **«LCOM1 = 0 στις κλάσεις με μία ευθύνη (κανόνες, στρατηγικές, πύλες) και όσο γίνεται
χαμηλό στις υπόλοιπες»**. Αυτό πρέπει να το συμφωνήσει η ομάδα, ή να ρωτηθεί η διδάσκουσα, πριν από το 2ο Παραδοτέο.
