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
