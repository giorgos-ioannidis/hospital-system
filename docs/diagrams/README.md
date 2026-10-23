# Διαγράμματα (2ο Παραδοτέο)

## Διαγράμματα κλάσεων
Βγήκαν από τον πραγματικό κώδικα (πεδία, μέθοδοι, σχέσεις, πολλαπλότητες). Getters/setters παραλείπονται.

| Αρχείο | Περιεχόμενο |
|---|---|
| `class-domain` (.png/.svg/.puml) | 1/3 Μοντέλο πεδίου: ιεραρχία χρηστών (DIT/NOCC), τμήματα, πρόγραμμα, άδειες, ραντεβού, φάκελος, κλίνες |
| `class-logic-scheduling` | 2/3 Παραγωγή προγράμματος: **Strategy** (`ScheduleGenerator`), **Chain of Responsibility** και **Template Method** (`Rule` → `HardRule`/`SoftRule` → 8 κανόνες) |
| `class-logic-services` | 3/3 Κρατήσεις, κλίνες, πρόσβαση φακέλου, **Factory** (`UserFactory`), **Observer** (`NotificationObserver`), πληρωμή |

Ξαναβγαίνουν από τα `.puml` με PlantUML και Graphviz:
`java -jar plantuml.jar -charset UTF-8 -tpng docs/diagrams/*.puml` (και ξανά με `-tsvg` για SVG)

## Στο Visual Paradigm
Το Visual Paradigm φτιάχνει το διάγραμμα κλάσεων **κατευθείαν από τον κώδικα Java** (reverse engineering), άρα ο
C δεν χρειάζεται να σχεδιάσει τις κλάσεις με το χέρι:

1. **Tools → Code → Instant Reverse…**, γλώσσα **Java**, και προσθήκη του φακέλου `src/main/java`.
2. Στο Model Explorer εμφανίζονται όλα τα πακέτα `gr.ihu.hospital.*`.
3. Για κάθε διάγραμμα: νέο **Class Diagram**, και σύρσιμο των κλάσεων που φαίνονται στο αντίστοιχο `.png` εδώ
   (π.χ. για το 2/3 το πακέτο `scheduling` και `scheduling.rules`). Οι σχέσεις κληρονομικότητας και συσχέτισης
   μπαίνουν αυτόματα.
4. Τακτοποίηση σύμφωνα με τις εικόνες αυτού του φακέλου και προσθήκη των σημειώσεων με τα patterns
   («Strategy», «Chain of Responsibility», «Template Method», «Factory», «Observer»).

Αν η έκδοση του Visual Paradigm έχει διαφορετικά μενού, η λειτουργία λέγεται «Instant Reverse» (Java source → UML).
Οι εικόνες εδώ είναι το πρότυπο για το τελικό αποτέλεσμα.

## Διαγράμματα ακολουθίας (6)
Στο `sequence/`, ένα ανά ΠΧ, με τα πραγματικά ονόματα κλάσεων και μεθόδων. Συμβολισμός: actor, boundary = οθόνη UI-xx,
control = controller/service/αλγόριθμος, entity = repositories/οντότητες. Οι ροές του εγγράφου είναι fragments
`alt`/`opt`/`loop` με τον αριθμό βήματος σε αγκύλες (π.χ. [7a1]).

| Αρχείο | ΠΧ | Επαύξηση | Τι δείχνει |
|---|---|---|---|
| `seq-1-uc01-login` | UC-01 Σύνδεση | 1 | bcrypt, ρόλος, κλείδωμα στην 3η αποτυχία, ξεκλείδωμα σε 15' |
| `seq-2-uc05-schedule` | UC-05 Παραγωγή Προγράμματος | 1 | Strategy (Fair/Greedy), Chain of Responsibility (8 κανόνες), backtracking, χειροκίνητη αλλαγή, έγκριση |
| `seq-3-uc09-absence` | UC-09 Δήλωση Απουσίας «include» UC-10 | 1 | βήματα 1–7 της προδιαγραφής, 4a1, 5a1, πρόταση αναπληρωτών |
| `seq-4-uc14-booking` | UC-14 Κράτηση «include» UC-13, «extend» UC-16 | 2 | βήματα 1–10 της προδιαγραφής, 7a1/7a2, 8a1/8a2, 8b1, κλείδωμα πόρων |
| `seq-5-uc27-notification` | UC-27 Αποστολή Ειδοποίησης | 2 | χρήστης «Χρόνος», @Scheduled, ουρά, επανάληψη ανά 30' έως 3 |
| `seq-6-uc19-record` | UC-19 Ιατρικό Ιστορικό | 3 | RecordAccessPolicy, καταγραφή κάθε πρόσβασης (REQUIRES_NEW), 403 |

Στο Visual Paradigm τα διαγράμματα ακολουθίας σχεδιάζονται χειροκίνητα, με πρότυπο αυτές τις εικόνες: νέο
**Sequence Diagram**, lifelines με τα ίδια ονόματα, μηνύματα με τη σειρά, και **Combined Fragment** (alt/opt/loop) για
τις εναλλακτικές ροές. Τα ονόματα των μεθόδων είναι τα ίδια με του κώδικα και του διαγράμματος κλάσεων.
