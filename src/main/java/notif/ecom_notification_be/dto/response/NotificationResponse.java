package notif.ecom_notification_be.dto.response;

import lombok.Data;
import notif.ecom_notification_be.model.NotificationChannel;
import notif.ecom_notification_be.model.NotificationStatus;
import notif.ecom_notification_be.model.NotificationType;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String orderId;
    private String message;
    private NotificationChannel channel;
    private NotificationStatus status;
    private LocalDateTime createdAt;
}
