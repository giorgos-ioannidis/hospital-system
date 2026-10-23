-- Demo νοσηλείες, ιστορικό και εξετάσεις ΜΟΝΟ για το dev.
-- patient3 (Δούκας) στο Καρδιολογικό, patient4 στο Παθολογικό, patient5 και patient1 στο Χειρουργικό (Χ-Α: 100% πληρότητα).
INSERT INTO admission (patient_id, bed_id, severity, isolation, admitted_at, admitted_by) VALUES
    (22, 1, 2, FALSE, DATEADD('DAY', -3, CURRENT_TIMESTAMP), 1),
    (23, 14, 1, FALSE, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1),
    (24, 19, 3, FALSE, DATEADD('DAY', -2, CURRENT_TIMESTAMP), 1),
    (20, 20, 2, FALSE, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1);

INSERT INTO medical_record (id, patient_id, created_at) VALUES
    (1, 22, DATEADD('DAY', -3, CURRENT_TIMESTAMP)), (2, 20, DATEADD('DAY', -1, CURRENT_TIMESTAMP));
INSERT INTO record_entry (record_id, author_id, entry_type, text, created_at) VALUES
    (1, 2, 'DIAGNOSIS', 'Κολπική μαρμαρυγή. Παρακολούθηση με Holter.', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
    (1, 2, 'ALLERGY', 'Αλλεργία στην πενικιλίνη.', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
    (1, 2, 'MEDICATION', 'Βισοπρολόλη 2,5 mg 1x1.', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
    (2, 20, 'PATIENT_NOTE', 'Υπέρταση από το 2020, παίρνω φάρμακα.', DATEADD('DAY', -1, CURRENT_TIMESTAMP));

INSERT INTO exam_order (patient_id, doctor_id, service_id, notes, status, ordered_at, result_text, result_at) VALUES
    (22, 2, 7, 'Πριν από την έναρξη αντιπηκτικής αγωγής', 'COMPLETED', DATEADD('DAY', -3, CURRENT_TIMESTAMP),
     'Hb 13,8 g/dL, WBC 7.200/μL, PLT 245.000/μL. Εντός φυσιολογικών ορίων.', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
    (22, 2, 8, NULL, 'ORDERED', DATEADD('DAY', -1, CURRENT_TIMESTAMP), NULL, NULL);

ALTER TABLE medical_record ALTER COLUMN id RESTART WITH 100;
