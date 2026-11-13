package notif.ecom_notification_be.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stands in for a real email/SMTP integration. Swap this out for one without
 * touching the Kafka listeners that depend on {@link NotificationSender}.
 */
@Slf4j
@Component
public class LoggingEmailNotificationSender implements NotificationSender {

    @Override
    public void send(String recipientId, String subject, String message) {
        log.info("[EMAIL -> {}] {}: {}", recipientId, subject, message);
    }
}
