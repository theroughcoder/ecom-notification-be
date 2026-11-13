package notif.ecom_notification_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notif.ecom_notification_be.dto.response.NotificationResponse;
import notif.ecom_notification_be.model.Notification;
import notif.ecom_notification_be.model.NotificationChannel;
import notif.ecom_notification_be.model.NotificationStatus;
import notif.ecom_notification_be.model.NotificationType;
import notif.ecom_notification_be.notification.NotificationSender;
import notif.ecom_notification_be.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final NotificationEmitterService notificationEmitterService;

    public void processEvent(UUID eventId, NotificationType type, String recipientId, String orderId,
                              String subject, String message) {
        if (notificationRepository.existsByEventId(eventId)) {
            log.info("Event {} already processed, skipping duplicate delivery", eventId);
            return;
        }

        Notification notification = new Notification();
        notification.setEventId(eventId);
        notification.setType(type);
        notification.setRecipientId(recipientId);
        notification.setOrderId(orderId);
        notification.setMessage(message);
        notification.setChannel(NotificationChannel.EMAIL);

        try {
            notificationSender.send(recipientId, subject, message);
            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception e) {
            log.error("Failed to send notification for event {}", eventId, e);
            notification.setStatus(NotificationStatus.FAILED);
        }

        notificationRepository.save(notification);
        notificationEmitterService.send(recipientId, toResponse(notification));
    }

    public List<NotificationResponse> getHistory(String recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setOrderId(notification.getOrderId());
        response.setMessage(notification.getMessage());
        response.setChannel(notification.getChannel());
        response.setStatus(notification.getStatus());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    /**
     * PaymentProcessedEvent carries no customerId of its own. order.placed is always
     * consumed first in the order flow, so its ORDER_PLACED notification (keyed by
     * customerId) is already on file by the time payment.processed arrives - reuse it
     * instead of falling back to the order id, so a customer's payment receipts still
     * show up in their notification history alongside their order confirmations.
     */
    public String resolveRecipientIdForOrder(String orderId, String fallback) {
        return notificationRepository.findFirstByOrderIdOrderByCreatedAtAsc(orderId)
                .map(Notification::getRecipientId)
                .orElse(fallback);
    }
}
