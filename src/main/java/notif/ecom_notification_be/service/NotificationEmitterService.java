package notif.ecom_notification_be.service;

import lombok.extern.slf4j.Slf4j;
import notif.ecom_notification_be.dto.response.NotificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Keeps SSE connections open per recipient so newly processed notifications
 * can be pushed to any browser tab subscribed to that recipient's stream.
 */
@Slf4j
@Service
public class NotificationEmitterService {

    private static final long TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private final Map<String, List<SseEmitter>> emittersByRecipient = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String recipientId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        List<SseEmitter> emitters = emittersByRecipient.computeIfAbsent(recipientId,
                key -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void send(String recipientId, NotificationResponse notification) {
        List<SseEmitter> emitters = emittersByRecipient.get(recipientId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException e) {
                log.debug("Dropping stale SSE connection for {}", recipientId);
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
