package notif.ecom_notification_be.kafka;

import com.yourapp.events.payment.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notif.ecom_notification_be.model.NotificationType;
import notif.ecom_notification_be.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment.processed", groupId = "notification-service")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent {} for order {} (status={})",
                event.eventId(), event.orderId(), event.status());

        boolean succeeded = event.status() == PaymentProcessedEvent.PaymentStatus.SUCCEEDED;
        String message = succeeded
                ? "We received your payment of %s %s for order #%s. Thank you!".formatted(
                        event.currency(), event.amount(), event.orderId())
                : "Your payment for order #%s failed: %s".formatted(
                        event.orderId(), event.failureReason());

        String recipientId = notificationService.resolveRecipientIdForOrder(event.orderId(), event.orderId());

        notificationService.processEvent(
                event.eventId(),
                succeeded ? NotificationType.PAYMENT_SUCCEEDED : NotificationType.PAYMENT_FAILED,
                recipientId,
                event.orderId(),
                succeeded ? "Payment receipt" : "Payment failed",
                message
        );
    }
}
