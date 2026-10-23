-- Δεδομένα αναφοράς: χρειάζονται σε κάθε περιβάλλον (dev και κοινή βάση).
INSERT INTO hospital (id, name, address) VALUES (1, 'Γενικό Νοσοκομείο Demo', 'Οδός Υγείας 1, Θεσσαλονίκη');

INSERT INTO department (id, hospital_id, name, floor) VALUES
    (1, 1, 'Καρδιολογικό', 1),
    (2, 1, 'Παθολογικό', 2),
    (3, 1, 'Χειρουργικό', 3);

INSERT INTO specialty (id, name) VALUES
    (1, 'Καρδιολογία'),
    (2, 'Παθολογία'),
    (3, 'Χειρουργική'),
    (4, 'Νοσηλευτική');

-- Ελάχιστη στελέχωση: 1 ιατρός της ειδικότητας του τμήματος + 1 νοσηλευτής ανά βάρδια, 1 ιατρός εφημερίας.
INSERT INTO staffing_requirement (department_id, specialty_id, shift_type, min_staff) VALUES
    (1, 1, 'MORNING', 1), (1, 1, 'ON_CALL', 1), (1, 4, 'MORNING', 1), (1, 4, 'AFTERNOON', 1), (1, 4, 'NIGHT', 1),
    (2, 2, 'MORNING', 1), (2, 2, 'ON_CALL', 1), (2, 4, 'MORNING', 1), (2, 4, 'AFTERNOON', 1), (2, 4, 'NIGHT', 1),
    (3, 3, 'MORNING', 1), (3, 3, 'ON_CALL', 1), (3, 4, 'MORNING', 1), (3, 4, 'AFTERNOON', 1), (3, 4, 'NIGHT', 1);

INSERT INTO room (id, department_id, name) VALUES
    (1, 1, 'Ιατρείο Κ1'), (2, 1, 'Ιατρείο Κ2'),
    (3, 2, 'Ιατρείο Π1'), (4, 2, 'Ιατρείο Π2'),
    (5, 3, 'Ιατρείο Χ1'), (6, 3, 'Αίθουσα επεμβάσεων Χ2');

INSERT INTO equipment (id, department_id, name) VALUES
    (1, 1, 'Υπερηχοκαρδιογράφος'),
    (2, 2, 'Υπερηχογράφος κοιλίας'),
    (3, 3, 'Ενδοσκόπιο');

INSERT INTO medical_service (id, department_id, name, price, equipment_id) VALUES
    (1, 1, 'Τακτική επίσκεψη καρδιολόγου', 30.00, NULL),
    (2, 1, 'Υπέρηχος καρδιάς', 60.00, 1),
    (3, 2, 'Τακτική επίσκεψη παθολόγου', 30.00, NULL),
    (4, 2, 'Υπέρηχος κοιλίας', 50.00, 2),
    (5, 3, 'Τακτική επίσκεψη χειρουργού', 30.00, NULL),
    (6, 3, 'Ενδοσκόπηση', 80.00, 3);

-- Τα IDENTITY συνεχίζουν μετά τα ρητά id.
ALTER TABLE hospital ALTER COLUMN id RESTART WITH 100;
ALTER TABLE department ALTER COLUMN id RESTART WITH 100;
ALTER TABLE specialty ALTER COLUMN id RESTART WITH 100;
ALTER TABLE room ALTER COLUMN id RESTART WITH 100;
ALTER TABLE equipment ALTER COLUMN id RESTART WITH 100;
ALTER TABLE medical_service ALTER COLUMN id RESTART WITH 100;
