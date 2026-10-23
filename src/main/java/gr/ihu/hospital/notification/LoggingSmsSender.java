package gr.ihu.hospital.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Δοκιμαστικό SMS: γράφεται στο log αντί να σταλεί. */
@Component
public class LoggingSmsSender implements SmsSender {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String phone, String text) {
        LOG.info("SMS προς {}: {}", phone, text);
    }
}
