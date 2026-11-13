package notif.ecom_notification_be.notification;

public interface NotificationSender {
    void send(String recipientId, String subject, String message);
}
