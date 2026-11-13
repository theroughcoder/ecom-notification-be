package notif.ecom_notification_be.kafka;

import com.yourapp.events.order.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notif.ecom_notification_be.model.NotificationType;
import notif.ecom_notification_be.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order.placed", groupId = "notification-service")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent {} for order {}", event.eventId(), event.orderId());
        String message = "Your order #%s has been placed. Total: %s %s.".formatted(
                event.orderId(), event.currency(), event.totalAmount());

        notificationService.processEvent(
                event.eventId(),
                NotificationType.ORDER_PLACED,
                event.customerId(),
                event.orderId(),
                "Order confirmation",
                message
        );
    }
}
