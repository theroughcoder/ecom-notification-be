package notif.ecom_notification_be.controller;

import lombok.RequiredArgsConstructor;
import notif.ecom_notification_be.dto.response.NotificationResponse;
import notif.ecom_notification_be.service.NotificationEmitterService;
import notif.ecom_notification_be.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationEmitterService notificationEmitterService;

    @GetMapping("/{userId}")
    public List<NotificationResponse> getHistory(@PathVariable String userId) {
        return notificationService.getHistory(userId);
    }

    @GetMapping(value = "/stream/{userId}", produces = "text/event-stream")
    public SseEmitter stream(@PathVariable String userId) {
        return notificationEmitterService.subscribe(userId);
    }
}
