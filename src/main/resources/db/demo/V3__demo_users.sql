-- Demo χρήστες και ώρες υποδοχής ΜΟΝΟ για τοπική χρήση (profile dev, H2). Κωδικός όλων: δες README.
INSERT INTO app_user (id, role, username, password_hash, first_name, last_name, email, phone, department_id, specialty_id, amka, gender, birth_date) VALUES
    (1, 'SUPERVISOR', 'supervisor', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Ελένη', 'Παπαδοπούλου', 'supervisor@demo.local', '2310000001', 1, NULL, NULL, NULL, NULL),
    (2, 'DOCTOR', 'doctor1', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Νίκος', 'Αλεξίου', 'doctor1@demo.local', '2310000101', 1, 1, NULL, NULL, NULL),
    (3, 'DOCTOR', 'doctor2', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Μαρία', 'Γεωργίου', 'doctor2@demo.local', '2310000102', 2, 2, NULL, NULL, NULL),
    (4, 'DOCTOR', 'doctor3', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Κώστας', 'Δημητρίου', 'doctor3@demo.local', '2310000103', 3, 3, NULL, NULL, NULL),
    (5, 'DOCTOR', 'doctor4', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Άννα', 'Ευαγγέλου', 'doctor4@demo.local', '2310000104', 1, 1, NULL, NULL, NULL),
    (6, 'DOCTOR', 'doctor5', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Γιάννης', 'Ζαχαρίου', 'doctor5@demo.local', '2310000105', 2, 2, NULL, NULL, NULL),
    (7, 'DOCTOR', 'doctor6', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Σοφία', 'Ηλιοπούλου', 'doctor6@demo.local', '2310000106', 3, 3, NULL, NULL, NULL),
    (8, 'DOCTOR', 'doctor7', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Πέτρος', 'Θεοδώρου', 'doctor7@demo.local', '2310000107', 1, 1, NULL, NULL, NULL),
    (9, 'DOCTOR', 'doctor8', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Δήμητρα', 'Ιωάννου', 'doctor8@demo.local', '2310000108', 2, 2, NULL, NULL, NULL),
    (10, 'NURSE', 'nurse1', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Χρήστος', 'Καλογήρου', 'nurse1@demo.local', '2310000201', 1, 4, NULL, NULL, NULL),
    (11, 'NURSE', 'nurse2', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Ειρήνη', 'Λαμπράκη', 'nurse2@demo.local', '2310000202', 2, 4, NULL, NULL, NULL),
    (12, 'NURSE', 'nurse3', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Βασίλης', 'Μαυρίδης', 'nurse3@demo.local', '2310000203', 3, 4, NULL, NULL, NULL),
    (13, 'NURSE', 'nurse4', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Κατερίνα', 'Νικολάου', 'nurse4@demo.local', '2310000204', 1, 4, NULL, NULL, NULL),
    (14, 'NURSE', 'nurse5', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Θανάσης', 'Ξανθόπουλος', 'nurse5@demo.local', '2310000205', 2, 4, NULL, NULL, NULL),
    (15, 'NURSE', 'nurse6', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Ελπίδα', 'Οικονόμου', 'nurse6@demo.local', '2310000206', 3, 4, NULL, NULL, NULL),
    (16, 'NURSE', 'nurse7', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Στέλιος', 'Πετρίδης', 'nurse7@demo.local', '2310000207', 1, 4, NULL, NULL, NULL),
    (17, 'NURSE', 'nurse8', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Αγγελική', 'Ρήγα', 'nurse8@demo.local', '2310000208', 2, 4, NULL, NULL, NULL),
    (18, 'NURSE', 'nurse9', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Μιχάλης', 'Σταύρου', 'nurse9@demo.local', '2310000209', 3, 4, NULL, NULL, NULL),
    (19, 'NURSE', 'nurse10', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Χαρά', 'Τσολάκη', 'nurse10@demo.local', '2310000210', 1, 4, NULL, NULL, NULL),
    (20, 'PATIENT', 'patient1', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Αλέξανδρος', 'Βλάχος', 'patient1@demo.local', '6900000001', NULL, NULL, '01011980001', 'M', DATE '1980-01-01'),
    (21, 'PATIENT', 'patient2', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Ιωάννα', 'Γκίκα', 'patient2@demo.local', '6900000002', NULL, NULL, '01011980002', 'F', DATE '1980-01-02'),
    (22, 'PATIENT', 'patient3', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Δημήτρης', 'Δούκας', 'patient3@demo.local', '6900000003', NULL, NULL, '01011980003', 'M', DATE '1980-01-03'),
    (23, 'PATIENT', 'patient4', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Ευτυχία', 'Καραγιάννη', 'patient4@demo.local', '6900000004', NULL, NULL, '01011980004', 'F', DATE '1980-01-04'),
    (24, 'PATIENT', 'patient5', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Λευτέρης', 'Μπακάλης', 'patient5@demo.local', '6900000005', NULL, NULL, '01011980005', 'M', DATE '1980-01-05');

-- Ώρες υποδοχής για τις επόμενες 14 ημέρες (SQL του H2, γιατί τρέχει μόνο στο dev).
INSERT INTO office_hours (doctor_id, room_id, work_date, start_time, end_time, slot_minutes)
SELECT d.doctor_id, d.room_id, DATEADD('DAY', x."X", CURRENT_DATE), d.start_time, d.end_time, 30
FROM SYSTEM_RANGE(1, 14) x
JOIN (VALUES (2, 1, TIME '09:00', TIME '13:00', 1), (3, 3, TIME '09:00', TIME '13:00', 1),
             (4, 5, TIME '09:00', TIME '13:00', 1), (5, 2, TIME '10:00', TIME '14:00', 2))
     AS d(doctor_id, room_id, start_time, end_time, pattern) ON TRUE
WHERE (d.pattern = 1 AND EXTRACT(ISO_DAY_OF_WEEK FROM DATEADD('DAY', x."X", CURRENT_DATE)) IN (1, 3, 5))
   OR (d.pattern = 2 AND EXTRACT(ISO_DAY_OF_WEEK FROM DATEADD('DAY', x."X", CURRENT_DATE)) IN (2, 4));

ALTER TABLE app_user ALTER COLUMN id RESTART WITH 100;
