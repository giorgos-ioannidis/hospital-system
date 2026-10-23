-- Αυτόματο ξεκλείδωμα μετά από 15 λεπτά: ο κλειδωμένος λογαριασμός δεν μένει για πάντα κλειδωμένος (DoS).
ALTER TABLE app_user ADD COLUMN locked_at TIMESTAMP;
