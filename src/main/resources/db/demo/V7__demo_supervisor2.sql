-- UC-03: δεύτερος Προϊστάμενος (Παθολογικό). Ο `supervisor` ενεργεί μόνο στο Καρδιολογικό.
INSERT INTO app_user (id, role, username, password_hash, first_name, last_name, email, phone, department_id, specialty_id, amka, gender, birth_date) VALUES
    (25, 'SUPERVISOR', 'supervisor2', '$2a$10$q/lCJRZJPpKd9r.huHCyx.qcvEcSawJwhjFX7Kps4gpdQ716NP31.', 'Αθηνά', 'Νικολάου', 'supervisor2@demo.local', '2310000002', 2, NULL, NULL, NULL, NULL);
