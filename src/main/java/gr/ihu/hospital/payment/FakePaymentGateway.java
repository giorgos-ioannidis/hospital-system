package gr.ihu.hospital.payment;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;

import gr.ihu.hospital.domain.appointment.Payment;

/** Δοκιμαστική πύλη: εγκρίνει πάντα, χωρίς πραγματική χρέωση και χωρίς στοιχεία κάρτας. */
@Component
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public String charge(Payment payment) {
        return "FAKE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
