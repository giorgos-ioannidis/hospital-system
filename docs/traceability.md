# Ιχνηλασιμότητα FR → UC → κλάσεις → tests → UI

Αρίθμηση ΠΧ και FR: **1ο Παραδοτέο** (28 ΠΧ, 36 FR). Κατάσταση: **Επαυξήσεις 1, 2 και 3 ολοκληρωμένες**, εκτός από τα Could UC-22 (Παρουσιολόγιο) και UC-24 (Αναφορές). 51 tests πράσινα.

## Κατάλογος οθονών (UI)
Το 1ο Παραδοτέο ορίζει ρητά τα UI-08, UI-11 και UI-12. Τα υπόλοιπα αριθμήθηκαν γύρω από αυτά. Τα templates έχουν το ίδιο όνομα (`templates/<φάκελος>/ui-xx-*.html`), ώστε τα screenshots να αντιστοιχούν ένα προς ένα.

| UI | Οθόνη | ΠΧ | Ρόλος |
|---|---|---|---|
| UI-01a / UI-01b | Σύνδεση / Αρχική ανά ρόλο | UC-01 | όλοι |
| UI-02 | Εγγραφή ασθενούς | UC-02 | Ασθενής |
| UI-03a / UI-03b | Λογαριασμοί προσωπικού / Επεξεργασία λογαριασμού | UC-03 | Προϊστάμενος |
| UI-04 | Προφίλ και κωδικός | UC-04 | όλοι |
| UI-05a / UI-05b | Παράμετροι παραγωγής / Ημερολόγιο προγράμματος | UC-05 | Προϊστάμενος |
| UI-06 | Αίτηση άδειας | UC-06 | Ιατρός, Νοσηλευτής |
| UI-07 | Εκκρεμείς άδειες με προειδοποίηση στελέχωσης | UC-07 | Προϊστάμενος |
| **UI-08** | Δήλωση απουσίας | UC-09 | Ιατρός, Νοσηλευτής |
| UI-09 | Το πρόγραμμά μου | UC-08 | Ιατρός, Νοσηλευτής |
| UI-10 | Αναπληρώσεις | UC-10 | Προϊστάμενος |
| **UI-11** | Κράτηση ραντεβού (με τον έλεγχο του UC-13 και τις εναλλακτικές της 7a1) | UC-14, UC-13 | Ασθενής |
| **UI-12** | Προεπισκόπηση ραντεβού | UC-14 | Ασθενής |
| UI-13 | Ώρες υποδοχής | UC-11 | Ιατρός |
| UI-14 | Τα ραντεβού μου (προβολή, αλλαγή, ακύρωση) | UC-15 | Ασθενής |
| UI-15 | Ραντεβού ιατρού (κατάσταση) | UC-15 | Ιατρός |
| UI-16 | Επιλογή τρόπου πληρωμής | UC-16 | Ασθενής |
| UI-17 | Ειδοποιήσεις | UC-27 | όλοι |
| UI-18a / UI-18b / UI-18c | Οι ασθενείς μου, αναζήτηση / Φάκελος / Ποιος είδε τον φάκελό μου | UC-19 | Ιατρός, Νοσηλευτής, Ασθενής |
| UI-19 | Εξετάσεις ιατρού: παραγγελία και αποτέλεσμα | UC-20, UC-21 | Ιατρός |
| UI-20 | Αποτελέσματα εξετάσεων | UC-21 | Ασθενής |
| UI-21 | Υπερωρίες | UC-23 | Προϊστάμενος |
| UI-22 | Χάρτης αιθουσών και τμημάτων | UC-25 | όλοι |
| UI-23 | Ιατροί και υπηρεσίες | UC-12 | όλοι |
| UI-24 | Κατανομή κλίνης | UC-17 | Προϊστάμενος |
| UI-25 | Πληρότητα θαλάμων | UC-18 | Προϊστάμενος |
| (κουμπιά «PDF» στα UI-09, UI-14, UI-18b, UI-19, UI-20) | Εξαγωγή PDF | UC-26 | όλοι |
| (φόρμα μέσα στο UI-14) | Αξιολόγηση επίσκεψης | UC-28 | Ασθενής |

## Επαύξηση 1 — Προσωπικό & Πρόγραμμα
| FR | UC | Κύριες κλάσεις | Tests | UI |
|---|---|---|---|---|
| FR-01 | UC-01 Σύνδεση | `SecurityConfig`, `AuthService`, `User.registerFailedLogin` | `LoginPageTest`, `LoginLockTest` (`login_locksAfterThreeFailures`), `RoleAccessTest` | UI-01a, UI-01b |
| FR-02 | UC-02 Εγγραφή Ασθενούς | `AccountService.registerPatient`, `Patient` | `PagesRenderTest` | UI-02 |
| FR-03 | UC-03 CRUD Λογαριασμού Προσωπικού | `AccountService` (create/staffAccounts/updateStaff/setActive), `UserFactory` (Factory) | `PagesRenderTest` | UI-03a, UI-03b |
| **FR-05** | UC-05 Παραγωγή Προγράμματος | `ScheduleGenerator` (Strategy) → `GreedyScheduleGenerator`, `FairScheduleGenerator`, `RuleChain` (CoR) + `Rule`/`HardRule`/`SoftRule` (Template Method), `ScheduleService.change` (χειροκίνητη αλλαγή) | **TC-1** `generator_neverAssignsTwoConsecutiveOnCalls`, **TC-2** `generator_neverSchedulesStaffOnApprovedLeave`, **TC-3** `generator_everyShiftMeetsMinimumStaffingOrIsReportedUncovered`, `generator_completesUnder10Seconds`, `weekendDifferenceAtMostOne`, `Increment1FlowTest` | UI-05a, UI-05b |
| FR-06 | UC-06 Υποβολή Αίτησης Άδειας | `LeaveService.request` | `Increment1FlowTest` | UI-06 |
| **FR-07** | UC-07 Έγκριση Αίτησης Άδειας | `StaffingChecker`, `LeaveService.approve` | `StaffingCheckerTest`, `Increment1FlowTest` | UI-07 |
| FR-08 | UC-08 Προβολή Προγράμματος | `ScheduleService.mySchedule` | `Increment1FlowTest` | UI-09 |
| FR-09 | UC-09 Δήλωση Απουσίας (4a1, 5a1, βήμα 6 → UC-10) | `AbsenceService.declare`, `ShiftsUncoveredEvent` | `PagesRenderTest` | UI-08 |
| **FR-10** | UC-10 Εύρεση Αντικαταστάτη | `ReplacementFinder`, `AbsenceService.uncovered/assignReplacement`, `ShiftAssignedEvent` | `replacementFinder_ranksByFewestHours`, `replacementFinder_looksInOtherDepartmentsWhenNoneAvailable`, `Increment1FlowTest` | UI-10 |

## Επαύξηση 2 — Ραντεβού & Πόροι
| FR | UC | Κύριες κλάσεις | Tests | UI |
|---|---|---|---|---|
| FR-04 | UC-04 Διαχείριση Προφίλ | `AccountService.updateContact/changePassword` | `PagesRenderTest` | UI-04 |
| FR-11 | UC-11 Ορισμός Ωρών Υποδοχής | `OfficeHoursService`, `OfficeHours`, `SlotGenerator` | `slotGenerator_splitsOfficeHoursIntoEqualSlots`, `Increment2FlowTest` | UI-13 |
| **FR-13** | UC-13 Έλεγχος Διαθεσιμότητας | `AvailabilityChecker`, `AlternativeSlotFinder` (7a1) | **TC-4** `availability_rejectsDoubleBookingOfDoctorRoomOrEquipment`, `availability_ignoresCancelledAppointments`, `availability_rejectsOutsideOfficeHoursShiftsAndAbsences`, `alternatives_areTheThreeNearestFreeSlots`, `sharedEquipmentIsNotDoubleBooked` | UI-11 |
| FR-14 | UC-14 Κράτηση Ραντεβού (7a1, 7a2, 8a1, 8b1) | `AppointmentService.book` (κλείδωμα ιατρού και εξοπλισμού), `Appointment` | `Increment2FlowTest`, `bookingCheckShowsPreviewOrAlternatives` | UI-11, UI-12 |
| FR-15, FR-16 | UC-15 RUD Ραντεβού | `AppointmentService.cancel/reschedule/changeStatus` | `cancel_rejectedWithinLast24Hours`, `doctorCompletesOnlyAfterStart`, `Increment2FlowTest` | UI-14, UI-15 |
| FR-17 | UC-16 Επιλογή Τρόπου Πληρωμής | `Payment`, `PaymentGateway` → `FakePaymentGateway` | `Increment2FlowTest` | UI-16 |
| FR-31 | UC-27 Αποστολή Ειδοποίησης (χρήστης «Χρόνος») | `NotificationObserver` (Observer), `NotificationService`, `DailyReminderJob` (`@Scheduled` 08:00 + αποστολή ανά λεπτό), `EmailSender`, `SmsSender` → `LoggingSmsSender` | `notification_retriesEvery30MinutesUpToThreeTimes`, `Increment2FlowTest` | UI-17 |
| NFR-01/03 | – | κλείδωμα γραμμών `@Lock(PESSIMISTIC_WRITE)` + συναλλαγή στην κράτηση | `Increment2FlowTest` (2η κράτηση ίδιας ώρας απορρίπτεται) | – |

## Επαύξηση 3 — Ιατρικός Φάκελος & Λοιπά
| FR | UC | Κύριες κλάσεις | Tests | UI |
|---|---|---|---|---|
| FR-20, FR-21 | UC-19 Ιατρικό Ιστορικό | `RecordAccessPolicy`, `MedicalRecordService`, `AccessLogger` (REQUIRES_NEW), `RecordAccessDeniedHandler`, `MedicalRecord`, `RecordEntry`, `AccessLog` | **TC-5** `medicalRecord_patientCannotAccessOtherPatientsRecord`, `roleDeniedAttemptsAreLoggedToo`, `staffSeeOnlyPatientsTheyTreatAndOnlyDoctorsWrite`, `RecordAccessPolicyTest` | UI-18a/b/c |
| FR-22 | UC-20 Παραγγελία Εξέτασης (κανόνας 7 ημερών) | `ExamService.order`, `ExamOrder.blocksRepeatOf`, `ExamEvent` | `examOrderRepeatRuleAndResult` | UI-19 |
| FR-23 | UC-21 Προβολή Αποτελεσμάτων | `ExamService.recordResult/mine/result` | `examOrderRepeatRuleAndResult` | UI-19, UI-20 |
| FR-25 | UC-23 Έγκριση Υπερωριών | `OvertimeCalculator`, `AdminService`, `Overtime` | `overtime_countsHoursAbove40PerWeek`, `overtimeIsComputedFromActiveScheduleAndApproved` | UI-21 |
| FR-27 | UC-25 Χάρτης | `HospitalInfoService.floors/todayIn` | `PagesRenderTest` | UI-22 |
| FR-12, FR-28 | UC-12 Πληροφορίες Νοσοκομείου | `HospitalInfoService.directory` | `PagesRenderTest` | UI-23 |
| **FR-18** | UC-17 Κατανομή Κλίνης | `BedAllocator`, `BedRequest`, `BedService.suggest/admit`, `Admission` | `bedAllocator_neverMixesGendersInAWard`, `bedAllocator_isolationOnlyInIsolationBedsAndSevereNearStation`, `bedAllocator_looksInOtherDepartmentsWhenFull`, `bedSuggestionRespectsGenderAndAdmissionUpdatesOccupancy` | UI-24 |
| FR-19 | UC-18 Πληρότητα Θαλάμων (> 90% κόκκινο) | `BedService.occupancy`, `Occupancy` | `bedSuggestionRespectsGenderAndAdmissionUpdatesOccupancy` | UI-25 |
| FR-29, FR-30 | UC-26 Εξαγωγή PDF | `PdfExporter` (OpenPDF, DejaVu Sans), `ExportService` | `examOrderRepeatRuleAndResult`, `PagesRenderTest` | PDF στα UI-09/14/18b/19/20 |
| FR-32 | UC-28 Αξιολόγηση Επίσκεψης | `Rating`, `AppointmentService.rate` | `rating_onlyOnceAndOnlyWhenCompleted` | UI-14 |
| FR-24 | UC-22 Παρουσιολόγιο | — | — | **Δεν υλοποιήθηκε (Could)** |
| FR-26 | UC-24 Διοικητικές Αναφορές | — | — | **Δεν υλοποιήθηκε (Could)** |

## Έλεγχος ασφαλείας (agent `security-ai-generated-code-auditor`, Επαύξηση 3)
Δεν βρέθηκε IDOR μεταξύ ασθενών. Διορθώθηκαν: καταγραφή και των απορρίψεων λόγω ρόλου (`RecordAccessDeniedHandler`), καταγραφή στην προβολή αποτελεσμάτων, ενιαίο 403 για ids που δεν είναι ασθενείς, μία γραμμή log ανά εξαγωγή, γενικό μήνυμα στην εγγραφή, αυτόματο ξεκλείδωμα σε 15', ρόλοι στα `/export/**`, αναζήτηση ασθενών με ΑΜΚΑ ή ≥ 3 γράμματα, secure cookie στο prod. Ανοιχτά (απόφαση ομάδας): επαλήθευση ταυτότητας ΑΜΚΑ πριν από νοσηλεία, διαχωρισμός καθηκόντων Προϊσταμένου, προεπιλεγμένο profile `dev`.

## Κανόνες προγράμματος (§7.1 του master prompt)
| Κανόνας | Τύπος | Κλάση |
|---|---|---|
| Ελάχιστη στελέχωση | Hard | `MinStaffingRule` |
| Μέγιστες ώρες/εβδομάδα | Hard | `MaxWeeklyHoursRule` |
| Ρεπό μετά από εφημερία | Hard | `RestAfterOnCallRule` |
| Άδεια, απουσία, **ώρες υποδοχής ιατρών** | Hard | `ApprovedLeaveRule` |
| Σωστή ειδικότητα | Hard | `SpecialtyRule` |
| ≥ 11 ώρες ανάπαυσης | Hard | `MinRestBetweenShiftsRule` (**προσθήκη, να μπει στο έγγραφο**) |
| Δίκαια Σ/Κ-αργίες (διαφορά ≤ 1) | Soft | `FairWeekendDistributionRule` |
| Ισορροπία ωρών | Soft | `BalancedHoursRule` |

## 5 υποχρεωτικά test cases
| # | Test | Κατάσταση |
|---|---|---|
| TC-1 | `generator_neverAssignsTwoConsecutiveOnCalls` | ✅ |
| TC-2 | `generator_neverSchedulesStaffOnApprovedLeave` | ✅ |
| TC-3 | `generator_everyShiftMeetsMinimumStaffingOrIsReportedUncovered` | ✅ |
| TC-4 | `availability_rejectsDoubleBookingOfDoctorRoomOrEquipment` | ✅ |
| TC-5 | `medicalRecord_patientCannotAccessOtherPatientsRecord` | ✅ |
