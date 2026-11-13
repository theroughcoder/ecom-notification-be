package notif.ecom_notification_be.repository;

import notif.ecom_notification_be.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByEventId(UUID eventId);
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    Optional<Notification> findFirstByOrderIdOrderByCreatedAtAsc(String orderId);
}
