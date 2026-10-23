package gr.ihu.hospital.payment;

import gr.ihu.hospital.domain.appointment.Payment;

/** Πύλη πληρωμών (FR-17). Επιστρέφει τον κωδικό της συναλλαγής. */
public interface PaymentGateway {

    String charge(Payment payment);
}
