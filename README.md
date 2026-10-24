# Πληροφοριακό Σύστημα Νοσοκομείου — Ομάδα 6

Μηχανική Λογισμικού (1742), ΔΙΠΑΕ. Spring Boot 3.5 · Java 21 · Thymeleaf + Bootstrap 5 · H2 (dev) / PostgreSQL-Supabase (prod) · Flyway.
Οδηγίες για το Claude Code και αρχιτεκτονική: [`CLAUDE.md`](CLAUDE.md).

## Προαπαιτούμενα
- JDK 21 ή νεότερο (`brew install openjdk@21` ή Temurin 21). Maven δεν χρειάζεται, υπάρχει το `./mvnw`.
- Git, VS Code με Extension Pack for Java + Spring Boot Extension Pack.

## Εκκίνηση (dev, H2 στη μνήμη, χωρίς setup)
```bash
./mvnw spring-boot:run      # http://localhost:8080
./mvnw test                 # όλα τα tests
```
Windows: `mvnw.cmd spring-boot:run`.

### Στο IntelliJ IDEA
1. **Open** τον φάκελο του project (αναγνωρίζεται ως Maven).
2. Τρέξτε την κλάση `HospitalApplication` (πράσινο ▶ δίπλα στο `main`). Ανοίγει στο `http://localhost:8080`.
3. Για αλλαγές σε templates/CSS χωρίς επανεκκίνηση (devtools): **Build → Build Project** (⌘F9), ή στο Run Configuration
   *Modify options → On frame deactivation: Update classes and resources*.

### Demo χρήστες (ΜΟΝΟ τοπικά, profile `dev`)
Κωδικός για όλους: `demo1234`

| Ρόλος | Χρήστες |
|---|---|
| Προϊστάμενος | `supervisor` |
| Ιατρός | `doctor1` … `doctor8` |
| Νοσηλευτής | `nurse1` … `nurse10` |
| Ασθενής | `patient1` … `patient5` |

Τα demo δεδομένα βρίσκονται στο `src/main/resources/db/demo/` και φορτώνονται **μόνο** στο `dev`. Στην κοινή βάση (`prod`) δεν μπαίνουν ποτέ.

Τρεις λάθος κωδικοί κλειδώνουν τον λογαριασμό. Ξεκλείδωμα: `supervisor` → Προσωπικό, ή επανεκκίνηση της εφαρμογής (η H2 ξαναφτιάχνεται).

## Τι κάνει ήδη (Επαυξήσεις 1, 2 και 3)
| Ρόλος | Οθόνες |
|---|---|
| Προϊστάμενος | Κατανομή κλινών (UI-24) · Πληρότητα θαλάμων (UI-25) · Υπερωρίες (UI-21) · Παραγωγή μηνιαίου προγράμματος (UI-05a) → ημερολόγιο με ακάλυπτες σε κόκκινο, χειροκίνητη αλλαγή, έγκριση (UI-05b) · Αιτήσεις αδειών με προειδοποίηση στελέχωσης (UI-07) · Αναπληρώσεις (UI-10) · CRUD λογαριασμών προσωπικού (UI-03a/b) |
| Ιατρός | Ώρες υποδοχής (UI-13) · Ραντεβού και κατάσταση (UI-15) · Εξετάσεις: παραγγελία και αποτέλεσμα (UI-19) · ό,τι ο Νοσηλευτής |
| Ιατρός / Νοσηλευτής | Ιατρικοί φάκελοι των ασθενών τους (UI-18a/b) · Το πρόγραμμά μου (UI-09) · Αίτηση άδειας (UI-06) · Δήλωση απουσίας (UI-08) |
| Ασθενής | Κράτηση ραντεβού (UI-11 → UI-12 → πληρωμή UI-16) · Τα ραντεβού μου: αλλαγή, ακύρωση έως 24 ώρες πριν, αξιολόγηση (UI-14) · Ο φάκελός μου και ποιος τον είδε (UI-18b/c) · Αποτελέσματα εξετάσεων (UI-20) |
| Όλοι | Χάρτης (UI-22) · Ιατροί και υπηρεσίες (UI-23) · PDF · Προφίλ (UI-04) · Ειδοποιήσεις (UI-17) · Εγγραφή ασθενούς (UI-02) |

Demo νοσηλείες: `patient3` στο Καρδιολογικό (με ιστορικό και εξετάσεις), `patient4` στο Παθολογικό, `patient5` και `patient1` στο Χειρουργικό (ο Θάλαμος Χ-Α είναι 100% γεμάτος). Ο `nurse1` (Καρδιολογικό) βλέπει τον φάκελο του `patient3`, αλλά όχι του `patient4`. Ο `patient1` δεν βλέπει κανέναν άλλο φάκελο (TC-5).

Στα demo δεδομένα οι `doctor1` έως `doctor3` έχουν ώρες υποδοχής Δευτέρα, Τετάρτη και Παρασκευή 09:00–13:00, και ο `doctor4` Τρίτη και Πέμπτη 10:00–14:00, για τις επόμενες 14 ημέρες. Οι ειδοποιήσεις στέλνονται κάθε λεπτό. Χωρίς `spring.mail.host` το email και το SMS γράφονται μόνο στο log.

## Κοινή βάση (prod, Supabase)
1. Αντέγραψε το `.env.example` σε `.env` (δεν ανεβαίνει στο Git) και συμπλήρωσε τα στοιχεία από Supabase → Connect → **Session pooler**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   DB_URL=jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require
   DB_USER=postgres.<project-ref>
   DB_PASSWORD=...
   ```
2. `set -a; source .env; set +a; ./mvnw spring-boot:run`
3. Το σχήμα το φτιάχνει μόνο το Flyway (`src/main/resources/db/migration`). Ποτέ χειροκίνητοι πίνακες.

## Δομή
`gr.ihu.hospital`: `config` · `domain` · `scheduling` (αλγόριθμος προγράμματος) · `booking` · `beds` · `payment` · `notification` · `pdf` · `repository` · `service` · `web`.
Templates: `templates/<ΠΧ>/ui-xx-*.html`, ίδια ονόματα με τις οθόνες του 1ου Παραδοτέου.

## Git
- `main` μόνο μέσω Pull Request (έλεγχος από A ή F).
- Branches: `feature/uc-03-schedule-generator`, `test/tc-4-double-booking`.
- Commits: `UC-03: add RestAfterOnCallRule`.

## Demo στο Render
`Dockerfile` + `render.yaml` (δωρεάν πλάνο, Frankfurt). Τρέχει με profile `dev`: H2 στη μνήμη με τους demo χρήστες,
χωρίς σύνδεση στη Supabase. Μετά από 15' χωρίς κίνηση το Render το σταματά. Η πρώτη επίσκεψη μετά παίρνει ~1 λεπτό
και τα δεδομένα ξεκινούν πάλι από την αρχή.
