# MASTER PROMPT — Πληροφοριακό Σύστημα Νοσοκομείου (Ομάδα 6, Μηχανική Λογισμικού 1742, ΔΙΠΑΕ)

> Αποθήκευσε αυτό το αρχείο και ως `CLAUDE.md` στη ρίζα του project. Το Claude Code το διαβάζει αυτόματα σε κάθε συνεδρία.
> Πηγή αλήθειας για τις απαιτήσεις είναι το έγγραφο «Πληροφοριακό Σύστημα Νοσοκομείου — 1ο Παραδοτέο» (22/10/2026). Αν κάτι εδώ συγκρούεται με το έγγραφο, ισχύει το έγγραφο και ενημερώνεται αυτό το αρχείο.

> **Αντιστοίχιση με το 1ο Παραδοτέο (υπερισχύει της §4/§5/§10 παρακάτω).** Το έγγραφο έχει **28 ΠΧ** (μία ανά στόχο) και όχι 14. Κύριες: UC-01 Σύνδεση, UC-02 Εγγραφή, UC-03 CRUD Λογαριασμού Προσωπικού, UC-04 Προφίλ, UC-05 Παραγωγή Προγράμματος, UC-06/07 Αίτηση/Έγκριση Άδειας, UC-08 Προβολή Προγράμματος, UC-09 Δήλωση Απουσίας, UC-10 Εύρεση Αντικαταστάτη, UC-11 Ώρες Υποδοχής, UC-12 Πληροφορίες, UC-13 Έλεγχος Διαθεσιμότητας, UC-14 Κράτηση, UC-15 RUD Ραντεβού, UC-16 Πληρωμή, UC-17/18 Κλίνες/Πληρότητα, UC-19 Ιατρικό Ιστορικό, UC-20/21 Εξετάσεις, UC-22 Παρουσιολόγιο, UC-23 Υπερωρίες, UC-24 Αναφορές, UC-25 Χάρτης, UC-26 PDF, UC-27 Ειδοποιήσεις (Χρόνος), UC-28 Αξιολόγηση. Επαύξηση 2 = UC-04, 11, 13, 14, 15, 16, 27. Επαύξηση 3 = UC-19, 20, 21, 23, 25 (Should), μετά τα Could UC-12, 17, 18, 22, 24, 26, 28. Οι οθόνες έχουν κωδικούς UI-01…UI-17: δες `docs/traceability.md`. Τα services/controllers κρατούν τα ονόματα της §4, με Javadoc τους κωδικούς του εγγράφου.

---

## 0. Ο ρόλος σου

Είσαι ο senior Java developer και αρχιτέκτονας της ομάδας. Χτίζεις μια **web εφαρμογή σε Java (Spring Boot)** για φοιτητική εργασία που βαθμολογείται με τρία κριτήρια:

1. **Όχι CRUD.** Η εφαρμογή πρέπει να έχει πραγματικούς αλγορίθμους (πρόγραμμα βαρδιών, αναπλήρωση, κρατήσεις χωρίς διπλοκρατήσεις, κατανομή κλινών).
2. **Μετρικές ποιότητας** με το MetricsCalculator της καθηγήτριας (LoC, NOC, CC, WMPC1, DIT, NOCC, CF, CBO, Fan In/Out, LCOM1).
3. **Συνέπεια με το 1ο Παραδοτέο.** Κάθε κλάση, οθόνη και test αντιστοιχεί σε FR, ΠΧ (UC) και οθόνη (UI-xx) του εγγράφου.

Πώς δουλεύεις:
- Γράφεις κώδικα που διαβάζεται από φοιτητές. Μικρές μέθοδοι, καθαρά ονόματα στα αγγλικά, σχόλια Javadoc στα ελληνικά όπου βοηθούν.
- Πριν από κάθε μεγάλη αλλαγή γράφεις σύντομο πλάνο. Μετά την αλλαγή τρέχεις `mvn test`.
- Δεν προσθέτεις λειτουργίες Won't (FR-33 έως FR-36).
- Δεν βάζεις ποτέ μυστικά (κωδικούς, keys) στον κώδικα ή στο Git.

---

## 1. Το έργο με μια ματιά

| Στοιχείο | Τιμή |
|---|---|
| Μάθημα | Μηχανική Λογισμικού (1742), ΔΙΠΑΕ — καθ. Ελβίρα-Μαρία Αρβανίτου |
| Ομάδα | Ομάδα-6, 6 μέλη (εγκρίθηκε από τη διδάσκουσα) |
| Θέμα | Πληροφοριακό Σύστημα Νοσοκομείου: βάρδιες/εφημερίες, άδειες, αναπληρώσεις, ραντεβού, πόροι, κλίνες |
| Μοντέλο | RUP (Σύλληψη, Επεξεργασία, Κατασκευή, Μετάβαση). Η Κατασκευή γίνεται σε 3 επαυξήσεις |
| Παραδοτέο 1 | 23/11 (υπόθεση): ανάλυση, ΠΧ, Gantt — **έχει γραφτεί** |
| Παραδοτέο 2 | 15/1 (υπόθεση): διάγραμμα κλάσεων, 6 διαγρ. ακολουθίας, κώδικας, 5 test cases, μετρικές, screenshots, video YouTube |
| Βαθμολογία | Εργασία 30%, γραπτή 70% (ανοιχτά βιβλία, χρειάζεται ≥ 4) |

### Ρόλοι (κωδικοί A–F, όπως στο WBS/Gantt)
| Κωδ. | Ρόλος | Ευθύνη στον κώδικα |
|---|---|---|
| A | Project Manager | README, issues, merges, τελικό έγγραφο |
| B | Αναλυτής | Προδιαγραφές ΠΧ, βοηθά στο domain model |
| C | Σχεδιαστής | Διάγραμμα κλάσεων και ακολουθίας (Visual Paradigm), αρχιτεκτονική πακέτων |
| D | Προγραμματίστρια (λογική) | `scheduling`, `booking`, `beds` — οι αλγόριθμοι |
| E | Προγραμματιστής (διεπαφή) | `web` controllers + Thymeleaf templates (UI-xx) |
| F | Ελεγκτής | JUnit tests, MetricsCalculator, έλεγχος ανά επαύξηση |

---

## 2. Τεχνικοί περιορισμοί από το έγγραφο (ΥΠΟΧΡΕΩΤΙΚΟΙ)

| NFR | Τι σημαίνει για τον κώδικα |
|---|---|
| **NFR-07** | **Java, αντικειμενοστρεφής.** Στόχοι: CC ≤ 10 ανά μέθοδο, WMPC1 ≤ 30 ανά κλάση, LCOM1 = 0 στις κλάσεις του πεδίου. Μέτρηση με το MetricsCalculator |
| **NFR-06** | Web εφαρμογή. Τρέχει στις 2 τελευταίες εκδόσεις Chrome, Firefox, Safari, χωρίς plugins |
| **NFR-05** | Responsive από 360 έως 1920 px (Bootstrap grid) |
| **NFR-02** | Έλεγχος ρόλου σε **κάθε** αίτημα. Ο Ασθενής βλέπει μόνο τον δικό του φάκελο. Κωδικοί με **bcrypt**. HTTPS στην παραγωγή. GDPR. Καταγραφή κάθε πρόσβασης σε φάκελο |
| **NFR-01** | Κράτηση < 2 s (95%, 100 χρήστες). Παραγωγή μηνιαίου προγράμματος ενός τμήματος < 10 s |
| **NFR-03** | Καμία απώλεια ραντεβού μετά από επανεκκίνηση: transactions, μόνιμη βάση |
| **NFR-04** | Νέος χρήστης κλείνει ραντεβού χωρίς βοήθεια σε < 15 λεπτά: απλές φόρμες, καθαρά μηνύματα |

### Κανόνες κώδικα που προκύπτουν από τις μετρικές
- **ΟΧΙ Lombok**, ΟΧΙ annotation processors που παράγουν κώδικα. Το MetricsCalculator μετρά τον πηγαίο κώδικα.
- Η λογική **δεν** μπαίνει σε controllers. Controller → Service → Domain/Algorithm.
- Κάθε μέθοδος με CC > 10 σπάει σε μικρότερες.
- Κάθε κλάση με WMPC1 > 30 σπάει σε δύο.
- **LCOM1 = 0 στο πεδίο:** οι κλάσεις αλγορίθμων (rules, generators, finders) πρέπει να είναι συνεκτικές, με λίγα πεδία που χρησιμοποιούνται από όλες τις μεθόδους. **Κίνδυνος:** οι JPA entities με getters/setters έχουν από τη φύση τους LCOM1 > 0. Μέτρησε νωρίς (τέλος Επαύξησης 1) και, αν χρειαστεί, πρότεινε στην ομάδα να περιοριστεί ο στόχος LCOM1 = 0 στις κλάσεις λογικής.
- Χρησιμοποίησε κληρονομικότητα όπου έχει νόημα, για DIT/NOCC (βλ. §6).

---

## 3. Τεχνολογίες (stack)

| Κομμάτι | Επιλογή | Σημειώσεις |
|---|---|---|
| Γλώσσα | **Java 21 (LTS)** | |
| Framework | **Spring Boot 3.x** (τελευταία σταθερή) | Spring Web, Spring Data JPA, Spring Security, Validation, Mail |
| Οθόνες | **Thymeleaf + Bootstrap 5** | Server-side, ώστε ο κώδικας να μένει Java. Bootstrap από WebJars ή CDN |
| Βάση (dev) | **H2** στη μνήμη | Profile `dev`, κανένα setup |
| Βάση (prod/κοινή) | **PostgreSQL στο Supabase** | Profile `prod`. Μία κοινή βάση για όλη την ομάδα |
| Migrations | **Flyway** | `src/main/resources/db/migration/V1__init.sql` κ.λπ. Ίδιο σχήμα σε H2 και Postgres |
| PDF (UC-13) | **OpenPDF** | Εξαγωγή προγράμματος, ραντεβού, φακέλου |
| Ειδοποιήσεις (UC-14) | Spring Mail + interface `SmsSender` | SMS με mock υλοποίηση (`LoggingSmsSender`). Χρονοπρογραμματισμός με `@Scheduled` καθημερινά 08:00 |
| Πληρωμές (FR-17) | Interface `PaymentGateway` | Μόνο mock υλοποίηση (`FakePaymentGateway`), καμία πραγματική χρέωση |
| Tests | **JUnit 5** + Spring Boot Test + AssertJ | |
| Build | **Maven** (wrapper `mvnw`) | |
| Έκδοση κώδικα | **Git + GitHub** | |

---

## 4. Δομή φακέλων (δημιούργησέ την ακριβώς έτσι)

```
hospital-system/
├── CLAUDE.md                     ← αντίγραφο αυτού του αρχείου
├── README.md                     ← οδηγίες εγκατάστασης για τα 6 μέλη
├── .gitignore                    ← target/, .idea/, .vscode/, *.iml, .env, application-local.yml
├── .env.example                  ← ονόματα μεταβλητών χωρίς τιμές
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
├── docs/
│   ├── paradoteo-1.pdf           ← το 1ο Παραδοτέο
│   ├── traceability.md           ← πίνακας FR → UC → κλάσεις → tests → UI
│   ├── diagrams/                 ← exports από Visual Paradigm (κλάσεων, 6 ακολουθίας)
│   ├── metrics/                  ← αποτελέσματα MetricsCalculator ανά επαύξηση
│   └── screenshots/
└── src/
    ├── main/
    │   ├── java/gr/ihu/hospital/
    │   │   ├── HospitalApplication.java
    │   │   ├── config/                   SecurityConfig, WebConfig, SchedulingConfig
    │   │   ├── domain/
    │   │   │   ├── user/                 User (abstract), HealthStaff (abstract), Doctor, Nurse, Supervisor, Patient, Role, UserFactory
    │   │   │   ├── organization/         Department, Specialty, Room, Equipment, Ward, Bed
    │   │   │   ├── schedule/             Schedule, ScheduleStatus, Shift, ShiftType, Assignment
    │   │   │   ├── leave/                LeaveRequest, LeaveType, LeaveStatus, Absence
    │   │   │   ├── appointment/          OfficeHours, TimeSlot, Appointment, AppointmentStatus, Payment, PaymentMethod, Rating
    │   │   │   ├── record/               MedicalRecord, RecordEntry, AccessLog, ExamOrder, ExamResult
    │   │   │   ├── admin/                Attendance, Overtime
    │   │   │   └── notification/         Notification, NotificationStatus, Channel
    │   │   ├── scheduling/               ← ΚΑΡΔΙΑ (FR-05, FR-10)
    │   │   │   ├── ScheduleGenerator.java          (interface — Strategy)
    │   │   │   ├── GreedyScheduleGenerator.java
    │   │   │   ├── FairScheduleGenerator.java
    │   │   │   ├── SchedulingContext.java          (προσωπικό, άδειες, παράμετροι μήνα/τμήματος)
    │   │   │   ├── ScheduleResult.java             (αναθέσεις + ακάλυπτες βάρδιες + παραβιάσεις)
    │   │   │   ├── rules/
    │   │   │   │   ├── Rule.java                   (abstract)
    │   │   │   │   ├── HardRule.java / SoftRule.java (abstract)
    │   │   │   │   ├── MinStaffingRule, MaxWeeklyHoursRule, RestAfterOnCallRule,
    │   │   │   │   │   ApprovedLeaveRule, SpecialtyRule        (Hard)
    │   │   │   │   ├── FairWeekendDistributionRule, BalancedHoursRule (Soft)
    │   │   │   │   └── RuleChain.java              (Chain of Responsibility)
    │   │   │   ├── ReplacementFinder.java          (UC-05)
    │   │   │   └── StaffingChecker.java            (FR-07: άδεια κάτω από ελάχιστη στελέχωση;)
    │   │   ├── booking/                  ← FR-13, FR-14, FR-11
    │   │   │   ├── AvailabilityChecker.java        (ιατρός + αίθουσα + εξοπλισμός)
    │   │   │   ├── SlotGenerator.java              (UC-06: ώρες υποδοχής → slots)
    │   │   │   └── AlternativeSlotFinder.java      (6a1: 3 πλησιέστερες ελεύθερες ώρες)
    │   │   ├── beds/                     ← FR-18 (Could)
    │   │   │   └── BedAllocator.java
    │   │   ├── payment/                  PaymentGateway, FakePaymentGateway
    │   │   ├── notification/             NotificationService, NotificationObserver (Observer), EmailSender, SmsSender, LoggingSmsSender, DailyReminderJob
    │   │   ├── pdf/                      PdfExporter (UC-13)
    │   │   ├── repository/               ένα Spring Data repository ανά aggregate
    │   │   ├── service/                  ένα service ανά ΠΧ: AuthService(UC-01), AccountService(UC-02), ScheduleService(UC-03),
    │   │   │                             LeaveService(UC-04), AbsenceService(UC-05), OfficeHoursService(UC-06), AppointmentService(UC-07),
    │   │   │                             BedService(UC-08), MedicalRecordService(UC-09), ExamService(UC-10), AdminService(UC-11),
    │   │   │                             HospitalInfoService(UC-12), ExportService(UC-13), NotificationService(UC-14)
    │   │   └── web/
    │   │       ├── controller/           ένας controller ανά ΠΧ
    │   │       ├── dto/                  φόρμες με Bean Validation
    │   │       └── advice/               GlobalExceptionHandler
    │   └── resources/
    │       ├── application.yml           κοινές ρυθμίσεις
    │       ├── application-dev.yml       H2
    │       ├── application-prod.yml      PostgreSQL από μεταβλητές περιβάλλοντος
    │       ├── db/migration/             V1__init.sql, V2__seed_demo.sql …
    │       ├── static/css, static/js, static/img
    │       └── templates/
    │           ├── layout.html           (navbar ανά ρόλο)
    │           ├── auth/        ui-01a-login.html, ui-01b-home-*.html
    │           ├── account/     ui-02a-register, ui-02b-staff-list, ui-02c-profile
    │           ├── schedule/    ui-03a-params, ui-03b-calendar, ui-03c-my-schedule
    │           ├── leave/       ui-04a-request, ui-04b-pending
    │           ├── absence/     ui-05a-declare, ui-05b-replacements
    │           ├── officehours/ ui-06
    │           ├── appointment/ ui-07a-book, ui-07b-preview, ui-07c-payment, ui-07d-mine
    │           ├── beds/        ui-08a-assign, ui-08b-occupancy
    │           ├── record/      ui-09a-search, ui-09b-record
    │           ├── exam/        ui-10a-order, ui-10b-results
    │           ├── admin/       ui-11a-overtime, ui-11b-attendance, ui-11c-reports
    │           └── info/        ui-12a-map, ui-12b-directory
    └── test/java/gr/ihu/hospital/
        ├── scheduling/           ← εδώ τα 5 βασικά test cases
        ├── booking/
        ├── security/
        └── service/
```

Τα ονόματα των templates ξεκινούν με τον κωδικό UI-xx του εγγράφου, ώστε τα screenshots να αντιστοιχούν ένα προς ένα.

---

## 5. Λειτουργικές απαιτήσεις → πού υλοποιούνται

| FR | Περιγραφή (σύντομα) | MoSCoW | UC | Κύρια κλάση |
|---|---|---|---|---|
| FR-01 | Sign in/out ανά ρόλο | M | UC-01 | SecurityConfig, AuthService |
| FR-02 | Εγγραφή Ασθενή | M | UC-02 | AccountService |
| FR-03 | Λογαριασμοί προσωπικού από Προϊστάμενο | M | UC-02 | AccountService, UserFactory |
| FR-04 | Προφίλ, επικοινωνία, κωδικός | S | UC-02 | AccountService |
| **FR-05** | **Αυτόματο μηνιαίο πρόγραμμα με κανόνες** | M | UC-03 | ScheduleGenerator + rules |
| FR-06 | Αίτηση άδειας | M | UC-04 | LeaveService |
| **FR-07** | **Έγκριση άδειας με προειδοποίηση στελέχωσης** | M | UC-04 | StaffingChecker |
| FR-08 | Προβολή/αναζήτηση προγράμματος | M | UC-03 | ScheduleService |
| FR-09 | Δήλωση ασθένειας | M | UC-05 | AbsenceService |
| **FR-10** | **Αυτόματη πρόταση αναπληρωτών** | M | UC-05 | ReplacementFinder |
| FR-11 | Ώρες υποδοχής | M | UC-06 | SlotGenerator |
| FR-12 | Κατάλογος ιατρών | C | UC-12 | HospitalInfoService |
| **FR-13** | **Διαθεσιμότητα ιατρού/αίθουσας/εξοπλισμού, χωρίς διπλοκρατήσεις** | M | UC-07 | AvailabilityChecker |
| FR-14 | Κράτηση με δέσμευση πόρων | M | UC-07 | AppointmentService |
| FR-15 | Ακύρωση/αλλαγή (κανόνας 24 ωρών) | M | UC-07 | AppointmentService |
| FR-16 | Κατάσταση ραντεβού από Ιατρό | M | UC-07 | AppointmentService |
| FR-17 | Πληρωμή (mock) — *εκκρεμεί αν μένει M ή γίνεται S* | M | UC-07 | PaymentGateway |
| **FR-18** | **Κατανομή κλινών (φύλο, βαρύτητα, απομόνωση)** | C | UC-08 | BedAllocator |
| FR-19 | Πληρότητα θαλάμων (>90% κόκκινο) | C | UC-08 | BedService |
| FR-20 | Ιατρικό ιστορικό (Ιατροί γράφουν, Νοσηλευτές διαβάζουν) | S | UC-09 | MedicalRecordService |
| FR-21 | Ο Ασθενής προσθέτει στο δικό του ιστορικό | S | UC-09 | MedicalRecordService |
| FR-22 | Παραπομπή εξετάσεων (κανόνας 7 ημερών) | S | UC-10 | ExamService |
| FR-23 | Αποτελέσματα εξετάσεων | S | UC-10 | ExamService |
| FR-24 | Παρουσιολόγιο | C | UC-11 | AdminService |
| FR-25 | Υπερωρίες | S | UC-11 | AdminService |
| FR-26 | Διοικητικές αναφορές | C | UC-11 | AdminService |
| FR-27 | Χάρτης νοσοκομείου | S | UC-12 | HospitalInfoService |
| FR-28 | Υπηρεσίες | C | UC-12 | HospitalInfoService |
| FR-29, FR-30 | Εξαγωγή PDF | C | UC-13 | PdfExporter |
| FR-31 | Ειδοποιήσεις SMS/Email (επανάληψη ανά 30', έως 3) | S | UC-14 | NotificationService, DailyReminderJob |
| FR-32 | Αξιολόγηση επίσκεψης 1–5 | C | UC-07 | AppointmentService |
| FR-33…36 | Won't | W | – | **Δεν υλοποιούνται** |

Οι ροές (βασικές και εναλλακτικές) κάθε ΠΧ είναι στην §7 του εγγράφου. Τα βήματα των controllers/services ακολουθούν αυτές τις ροές ένα προς ένα.

---

## 6. Μοντέλο πεδίου και design patterns

### Ιεραρχίες (για DIT / NOCC)
```
User (abstract)                       ← σύνδεση, προφίλ, εξαγωγή PDF
├── Supervisor
├── Patient
└── HealthStaff (abstract)            ← πρόγραμμα, άδειες, απουσίες, φάκελος
    ├── Doctor                        ← ώρες υποδοχής, ραντεβού, εξετάσεις
    └── Nurse

Rule (abstract)
├── HardRule (abstract) → MinStaffingRule, MaxWeeklyHoursRule, RestAfterOnCallRule, ApprovedLeaveRule, SpecialtyRule
└── SoftRule (abstract) → FairWeekendDistributionRule, BalancedHoursRule
```
Ο «Χρόνος» (actor του UC-14) υλοποιείται ως `DailyReminderJob` με `@Scheduled`.

JPA: single-table inheritance για τους χρήστες (`@Inheritance(strategy = SINGLE_TABLE)` με στήλη `role`).

### Design patterns (για το 2ο παραδοτέο, εβδομάδα 11)
| Pattern | Πού |
|---|---|
| **Strategy** | `ScheduleGenerator` → `GreedyScheduleGenerator`, `FairScheduleGenerator` |
| **Chain of Responsibility** | `RuleChain`: κάθε `Rule` ελέγχει μια ανάθεση και περνά στον επόμενο |
| **Observer** | Αλλαγή προγράμματος/ραντεβού → `NotificationObserver` → ειδοποίηση |
| **Factory** | `UserFactory.create(role, data)` για τους λογαριασμούς προσωπικού (FR-03) |
| (προαιρετικά) **Template Method** | `Rule.evaluate()` που καλεί `check()` των υποκλάσεων |

---

## 7. Οι αλγόριθμοι (προδιαγραφή)

### 7.1 Παραγωγή προγράμματος (FR-05, UC-03)
**Είσοδος:** μήνας, τμήμα, ελάχιστη στελέχωση ανά βάρδια (πρωί/απόγευμα/νύχτα/εφημερία), προσωπικό του τμήματος με ειδικότητα, εγκεκριμένες άδειες, ώρες υποδοχής ιατρών.

**Σκληροί κανόνες (ποτέ δεν παραβιάζονται):**
- ελάχιστη στελέχωση ανά βάρδια και ειδικότητα
- μέγιστες ώρες ανά εβδομάδα (παραμετρικό, π.χ. 48)
- ρεπό την επόμενη μέρα μετά από εφημερία (όχι 2 συνεχόμενες)
- κανείς σε εγκεκριμένη άδεια
- σωστή ειδικότητα για τη βάρδια

**Μαλακοί κανόνες (βαθμολογία):** δίκαιη κατανομή Σαββατοκύριακων/αργιών (διαφορά ≤ 1), ισορροπία ωρών μεταξύ μελών.

**Αλγόριθμος:** greedy με βαθμολόγηση.
1. Για κάθε μέρα και βάρδια, οι υποψήφιοι είναι όσοι περνούν όλους τους HardRule.
2. Τους κατατάσσεις με το σκορ των SoftRule (λιγότερες ώρες ή λιγότερα Σ/Κ = υψηλότερο σκορ).
3. Αναθέτεις μέχρι να καλυφθεί η στελέχωση.
4. Αν μια βάρδια δεν καλύπτεται, κάνεις περιορισμένο backtracking: ξαναδοκιμάζεις τις 1–2 προηγούμενες μέρες.
5. Αν πάλι δεν καλύπτεται, μπαίνει στο `ScheduleResult.uncovered` (UI-03b σε κόκκινο, ροή 6a1).

**Έξοδος:** `ScheduleResult` με τις αναθέσεις, τις ακάλυπτες βάρδιες και τις παραβιάσεις μαλακών κανόνων. Το πρόγραμμα σώζεται ως DRAFT και γίνεται ACTIVE μετά την έγκριση του Προϊσταμένου.

Η χειροκίνητη αλλαγή (ροή 8a1) περνά από το **ίδιο** `RuleChain` και επιστρέφει προειδοποιήσεις.

**Απόδοση:** < 10 s για ένα τμήμα (NFR-01). Πρόσθεσε test που το ελέγχει με 30 μέλη και 31 μέρες.

### 7.2 Αναπλήρωση (FR-10, UC-05)
Για κάθε ακάλυπτη βάρδια βρίσκεις μέλη με την ίδια ειδικότητα, χωρίς βάρδια, άδεια ή ρεπό εκείνη την ώρα, που περνούν τους HardRule. Τα ταξινομείς με λιγότερες ώρες και λιγότερες υπερωρίες του μήνα. Αν δεν βρεθεί κανείς στο τμήμα, ψάχνεις σε άλλα τμήματα με την ίδια ειδικότητα (6a1). Ο Προϊστάμενος επιλέγει και ειδοποιείται ο αντικαταστάτης (Observer).

### 7.3 Έλεγχος στελέχωσης σε άδεια (FR-07)
Το `StaffingChecker` υπολογίζει, για κάθε βάρδια του διαστήματος της άδειας, αν το τμήμα πέφτει κάτω από την ελάχιστη στελέχωση. Επιστρέφει τις βάρδιες που επηρεάζονται, και αυτές εμφανίζονται ως προειδοποίηση στο UI-04b. Αν η άδεια εγκριθεί ενώ υπάρχει ενεργό πρόγραμμα, οι βάρδιες του αιτούντα γίνονται ακάλυπτες.

### 7.4 Κρατήσεις χωρίς διπλοκράτηση (FR-13, UC-07)
Το `AvailabilityChecker.isFree(doctor, room, equipment, start, end)` ελέγχει επικάλυψη διαστημάτων με υπάρχοντα ραντεβού, βάρδιες/εφημερίες και άδειες. Η καταχώριση γίνεται μέσα σε **transaction** με κλείδωμα (pessimistic lock ή unique constraint), ώστε δύο ταυτόχρονες κρατήσεις να μην πάρουν το ίδιο slot. Αν κάτι δεν είναι ελεύθερο, το `AlternativeSlotFinder` επιστρέφει τις 3 πλησιέστερες ώρες όπου όλοι οι πόροι είναι ελεύθεροι (6a1). Η ακύρωση επιτρέπεται μόνο αν απομένουν ≥ 24 ώρες.

### 7.5 Ώρες υποδοχής (FR-11, UC-06)
Απορρίπτονται ώρες που συγκρούονται με βάρδια, εφημερία, άδεια ή κράτηση αίθουσας. Από το διάστημα και τη διάρκεια ραντεβού παράγονται τα `TimeSlot`. Η διαγραφή ωρών με κλεισμένα ραντεβού θέλει επιβεβαίωση, ακυρώνει τα ραντεβού και ειδοποιεί τους ασθενείς.

### 7.6 Κατανομή κλινών (FR-18, Could)
Οι ελεύθερες κλίνες του τμήματος βαθμολογούνται με: φύλο θαλάμου (σκληρό), απομόνωση (σκληρό), βαρύτητα 3 → κοντά στον σταθμό νοσηλευτών (μαλακό). Επιστρέφονται η καλύτερη και 2 εναλλακτικές. Αν δεν υπάρχει, προτείνονται κλίνες άλλων τμημάτων.

---

## 8. Ασφάλεια (UC-01, NFR-02)
- Spring Security με form login, `BCryptPasswordEncoder`.
- Ρόλοι: `ROLE_SUPERVISOR`, `ROLE_DOCTOR`, `ROLE_NURSE`, `ROLE_PATIENT`. Πρόσβαση σε URL με βάση τον ρόλο και `@PreAuthorize` στα services.
- **3 αποτυχημένες προσπάθειες → κλείδωμα λογαριασμού** (ροή 5b1): μετρητής στον `User` και `AuthenticationFailureHandler`.
- **Ιατρικός φάκελος:** ο Ασθενής βλέπει μόνο τον δικό του. Το προσωπικό βλέπει μόνο αν ο ασθενής νοσηλεύεται στο τμήμα του ή έχει ραντεβού μαζί του. **Κάθε** πρόσβαση και απόπειρα γράφεται στο `AccessLog`.
- CSRF ενεργό (Thymeleaf το βάζει αυτόματα στις φόρμες).
- Στην παραγωγή μόνο HTTPS.
- ΑΜΚΑ: 11 ψηφία, μοναδικό.

---

## 9. Τα 5 υποχρεωτικά test cases (JUnit 5)

| # | Test | Τι αποδεικνύει | FR |
|---|---|---|---|
| TC-1 | `generator_neverAssignsTwoConsecutiveOnCalls` | Ρεπό μετά από εφημερία | FR-05 |
| TC-2 | `generator_neverSchedulesStaffOnApprovedLeave` | Σεβασμός αδειών | FR-05 |
| TC-3 | `generator_everyShiftMeetsMinimumStaffingOrIsReportedUncovered` | Στελέχωση ή σωστή αναφορά κενού | FR-05 |
| TC-4 | `availability_rejectsDoubleBookingOfDoctorRoomOrEquipment` | Καμία διπλοκράτηση | FR-13 |
| TC-5 | `medicalRecord_patientCannotAccessOtherPatientsRecord` | 0 προσβάσεις σε ξένο φάκελο | NFR-02, FR-20 |

Επιπλέον (όχι υποχρεωτικά): `replacementFinder_ranksByFewestHours`, `weekendDifferenceAtMostOne`, `login_locksAfterThreeFailures`, `cancel_rejectedWithinLast24Hours`, `generator_completesUnder10Seconds`.

Τα tests του `scheduling` και του `booking` είναι **καθαρά unit tests χωρίς Spring και χωρίς βάση**, ώστε να τρέχουν σε λίγα ms. Αυτό σημαίνει ότι οι αλγόριθμοι παίρνουν τα δεδομένα ως αντικείμενα και όχι από repositories.

---

## 10. Σειρά υλοποίησης (ακολουθεί τις επαυξήσεις του RUP)

### Βήμα 0 — Σκελετός (κάν' το πρώτο)
1. Δημιούργησε τη δομή φακέλων της §4 και το `pom.xml` (Spring Boot 3.x, Java 21, web, thymeleaf, security, data-jpa, validation, mail, h2, postgresql, flyway-core, flyway-database-postgresql, openpdf, spring-boot-starter-test, spring-security-test, webjars bootstrap).
2. `application.yml`, `application-dev.yml` (H2, ενεργό από προεπιλογή), `application-prod.yml` (PostgreSQL από env vars).
3. `.gitignore`, `.env.example`, `README.md` με εντολές εκκίνησης.
4. `V1__init.sql` με όλους τους πίνακες της Επαύξησης 1 και `V2__seed_demo.sql` με demo δεδομένα: 1 νοσοκομείο, 3 τμήματα, 1 Προϊστάμενος, 8 Ιατροί, 10 Νοσηλευτές, 5 Ασθενείς. Ένας demo χρήστης ανά ρόλο, με κωδικούς **μόνο** για τοπική χρήση, γραμμένους στο README.
5. Έλεγχος: το `./mvnw spring-boot:run` ανοίγει τη φόρμα σύνδεσης στο `http://localhost:8080`.

### Βήμα 1 — Επαύξηση 1: Προσωπικό & Πρόγραμμα (UC-01 έως UC-05, όλα Must)
Πρώτα το domain και οι αλγόριθμοι με τα tests TC-1 έως TC-3. Μετά τα services. Μετά οι controllers και τα templates UI-01 έως UI-05.

### Βήμα 2 — Επαύξηση 2: Ραντεβού (UC-06, UC-07, UC-14)
AvailabilityChecker + TC-4, SlotGenerator, AppointmentService, mock πληρωμή, ειδοποιήσεις με Observer και `@Scheduled`.

### Βήμα 3 — Επαύξηση 3: Φάκελος & λοιπά (UC-09 έως UC-12, μετά Could UC-08, UC-13)
MedicalRecordService + AccessLog + TC-5, εξετάσεις, διοικητικός έλεγχος, πληροφορίες. Αν μείνει χρόνος: κλίνες και PDF. Αν η Επαύξηση 3 καθυστερήσει (διακοπές 24/12–6/1), **κόβονται πρώτα τα Could**.

### Βήμα 4 — Μετάβαση
Μετρικές με MetricsCalculator → `docs/metrics/`, διορθώσεις CC/WMPC1, screenshots ανά UI-xx → `docs/screenshots/`, tutorial video.

Στο τέλος κάθε βήματος:
- `./mvnw test` πράσινο
- ενημέρωση του `docs/traceability.md`
- σύντομη σύνοψη για την ομάδα: τι έγινε, τι μένει, ποιο μέλος το παίρνει

---

## 11. Εργαλεία, connectors και πλατφόρμες

### 11.1 Στον υπολογιστή (κάθε μέλος)
| Εργαλείο | Εγκατάσταση (macOS) | Γιατί |
|---|---|---|
| JDK 21 | `brew install openjdk@21` ή adoptium.net (Temurin 21) | Μεταγλώττιση |
| Maven | `brew install maven` (ή μόνο το `./mvnw`) | Build |
| Git | `xcode-select --install` ή `brew install git` | Έκδοση κώδικα |
| **VS Code** | code.visualstudio.com | Κύριος editor |
| — Extension Pack for Java | Microsoft | Java, debug, tests |
| — Spring Boot Extension Pack | VMware | Spring Boot Dashboard, εκτέλεση |
| — Claude Code | Anthropic | Γράφει κώδικα μέσα στο VS Code με αυτό το CLAUDE.md |
| (προαιρετικά) IntelliJ IDEA | ήδη εγκατεστημένο | Εναλλακτικός editor |
| Visual Paradigm | από το ΔΙΠΑΕ / Community | Διαγράμματα ΠΧ, κλάσεων, ακολουθίας |
| ProjectLibre | projectlibre.com | Gantt από το WBS (§9 του εγγράφου) |
| MetricsCalculator (.jar) | Moodle μαθήματος | Μετρικές 2ου παραδοτέου |

### 11.2 Connectors στο Claude
| Connector | Τι κάνει για το project | Πότε |
|---|---|---|
| **Φάκελος υπολογιστή** (Claude desktop → «+» → «Add folder») | Το Claude γράφει και διαβάζει τα αρχεία στο `~/Projects/hospital-system` | Από το Βήμα 0 |
| **Supabase** (συνδεδεμένο) | PostgreSQL στο cloud, κοινή για την ομάδα. Εργαλεία: `list_projects`, `create_project`, `get_project_url`, `list_tables`, `apply_migration`, `execute_sql`, `get_advisors` (έλεγχοι ασφάλειας/απόδοσης) | Μετά το Βήμα 1, όταν σταθεροποιηθεί το σχήμα |
| **GitHub** | Κοινό repo, ένα branch ανά μέλος, Pull Requests | Μετά το Βήμα 0 |
| Google Drive (συνδεδεμένο) | Το 1ο Παραδοτέο και τα έγγραφα της ομάδας | Όποτε χρειαστεί |
| Gmail / Calendar (συνδεδεμένα) | Ενημερώσεις ομάδας, προθεσμίες στο ημερολόγιο | Προαιρετικά |

**Σημείωση:** το Vercel **δεν** τρέχει Spring Boot. Αν χρειαστεί δημόσιο link για την παρουσίαση, προτίμησε μια πλατφόρμα που τρέχει Java σε Docker, ή απλώς τοπική εκτέλεση στο laptop της παρουσίασης.

### 11.3 Σύνδεση με Supabase (profile `prod`)
1. Δημιούργησε project στο Supabase (region: Europe, π.χ. Frankfurt).
2. Από το Connect → **Session pooler** πάρε host, port, user. Το direct connection είναι IPv6 και συχνά δεν δουλεύει από οικιακά δίκτυα.
3. Βάλε τα στοιχεία σε μεταβλητές περιβάλλοντος, **ποτέ** στον κώδικα:
   ```
   SPRING_PROFILES_ACTIVE=prod
   DB_URL=jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require
   DB_USER=postgres.<project-ref>
   DB_PASSWORD=<από το Supabase>
   ```
4. `application-prod.yml`:
   ```yaml
   spring:
     datasource:
       url: ${DB_URL}
       username: ${DB_USER}
       password: ${DB_PASSWORD}
     jpa:
       hibernate:
         ddl-auto: validate
     flyway:
       enabled: true
   ```
5. Το σχήμα το φτιάχνει **μόνο το Flyway**. Όχι χειροκίνητοι πίνακες και όχι `ddl-auto: update`.
6. Κάθε μέλος κρατά τα δικά του στοιχεία σε `.env` ή στο Run Configuration του VS Code. Το `.env` είναι στο `.gitignore`.

### 11.4 Git για 6 άτομα
- `main`: πάντα λειτουργικό, μόνο μέσω Pull Request. Το ελέγχει ο A ή ο F.
- Branches: `feature/uc-03-schedule-generator`, `feature/ui-07-booking`, `test/tc-4-double-booking`.
- Commits: `UC-03: add RestAfterOnCallRule` (κωδικός ΠΧ πρώτα).
- Ένα GitHub Issue ανά δραστηριότητα του WBS (3.1, 3.2 …) με το μέλος (A–F) ως assignee.

---

## 12. Ορισμός «ολοκληρώθηκε» (Definition of Done) για κάθε ΠΧ
- [ ] Οι ροές του εγγράφου (βασική + εναλλακτικές) λειτουργούν στην οθόνη UI-xx
- [ ] Ο έλεγχος ρόλου υπάρχει και στο service, όχι μόνο στο URL
- [ ] Unit tests για τη λογική, πράσινο `./mvnw test`
- [ ] Καμία μέθοδος με CC > 10
- [ ] Ενημερωμένο `docs/traceability.md`
- [ ] Screenshot στο `docs/screenshots/ui-xx.png`

---

## 13. Ανοιχτά θέματα (από την §11 του εγγράφου)
- Πραγματικές ημερομηνίες Παραδοτέων 1 και 2 (τώρα: υποθέσεις 23/11 και 15/1).
- Τελική αντιστοίχιση μελών–ρόλων (A–F).
- FR-17 (πληρωμή): μένει Must ή γίνεται Should;
- Έλεγχος από την ομάδα των κανόνων 24 ωρών (ακύρωση), 7 ημερών (εξετάσεις), 90% (πληρότητα).
- Μεταφορά του WBS στο ProjectLibre.
- Αν ο στόχος LCOM1 = 0 ισχύει για όλες τις κλάσεις του πεδίου ή μόνο για τις κλάσεις λογικής (βλ. §2).
- **Επαύξηση 1:** προστέθηκε 6ος σκληρός κανόνας `MinRestBetweenShiftsRule` (≥ 11 ώρες ανάπαυσης, καμία επικάλυψη βαρδιών). Να μπει και στο έγγραφο (§7.1) ή να αφαιρεθεί.
- **Επαύξηση 1:** η εφημερία ορίστηκε 24ωρη (08:00 → 08:00) και οι βάρδιες 8ωρες (07–15, 15–23, 23–07). Να επιβεβαιωθεί με το έγγραφο.
- **Demo δεδομένα:** με 2–3 ιατρούς ανά τμήμα, 24ωρη εφημερία κάθε μέρα και 48 ώρες/εβδομάδα το πρόγραμμα βγαίνει αναγκαστικά με ακάλυπτες βάρδιες.
- **Δεδομένα:** τα δεδομένα αναφοράς (τμήματα, ειδικότητες, αίθουσες, εξοπλισμός, υπηρεσίες, στελέχωση) είναι στο `db/migration/V2`. Μόνο οι demo χρήστες και οι demo ώρες υποδοχής είναι στο `db/demo/V3` (μόνο στο dev). Στην κοινή βάση χρειάζεται τρόπος να μπει ο πρώτος Προϊστάμενος.
- **Ώρες υποδοχής:** ορίζονται ανά συγκεκριμένη ημέρα (όχι εβδομαδιαία επανάληψη) και μπαίνουν ως «μη διαθεσιμότητα» στον αλγόριθμο βαρδιών.
- **Ερμηνείες κανόνων (το έγγραφο δεν τους ορίζει):** «7 ημερών» = η ίδια εξέταση δεν ξαναπαραγγέλνεται για τον ίδιο ασθενή μέσα σε 7 ημέρες. «90%» = θάλαμος με πληρότητα > 90% σε κόκκινο. «Υπερωρίες» = ώρες πάνω από 40 ανά εβδομάδα. «Φύλο θαλάμου» = προκύπτει από τους νοσηλευόμενους.
- **Ασφάλεια (ανοιχτά):** επαλήθευση ταυτότητας ΑΜΚΑ πριν δεθεί νοσηλεία ή ιστορικό σε λογαριασμό που φτιάχτηκε με αυτο-εγγραφή, διαχωρισμός καθηκόντων (ο Προϊστάμενος φτιάχνει ιατρούς και κάνει εισαγωγές), προεπιλεγμένο profile `dev` (στο deploy να οριστεί ρητά `SPRING_PROFILES_ACTIVE=prod`).
- **Migrations:** από το V4 και μετά δεν αλλάζουν τα παλιά αρχεία· κάθε αλλαγή σχήματος = νέο `V<n>__*.sql`.
- **Κώδικας σε σύνταξη Java 8** (όχι records, `var`, switch expressions), για να τον διαβάζει σίγουρα το MetricsCalculator.

---

## 14. Πρώτη εντολή

> Διάβασε όλο αυτό το αρχείο. Μετά εκτέλεσε το **Βήμα 0** της §10 στον φάκελο του project: δομή φακέλων, `pom.xml`, ρυθμίσεις, migrations με demo δεδομένα, README. Στο τέλος τρέξε `./mvnw test` και `./mvnw spring-boot:run` και επιβεβαίωσε ότι ανοίγει η φόρμα σύνδεσης. Μετά σταμάτα και γράψε μου τι έγινε και τι ακολουθεί (Επαύξηση 1).
