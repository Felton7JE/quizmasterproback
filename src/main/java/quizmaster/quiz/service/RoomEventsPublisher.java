package quizmaster.quiz.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomEventsPublisher {
    private final Map<String, List<SseEmitter>> emittersByRoom = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String roomCode) {
        SseEmitter emitter = new SseEmitter(0L); // sem timeout
        emittersByRoom.computeIfAbsent(roomCode, k -> new ArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(roomCode, emitter));
        emitter.onTimeout(() -> remove(roomCode, emitter));
        emitter.onError(e -> remove(roomCode, emitter));
        return emitter;
    }

    public void publishRoomStarted(String roomCode, LocalDateTime startsAt, Long gameId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "room_started");
        payload.put("roomCode", roomCode);
        payload.put("startsAt", startsAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        payload.put("gameId", gameId);
        send(roomCode, payload);
    }

    public void publishRoomUpdated(String roomCode) {
        Map<String, Object> payload = Map.of("type", "room_updated", "roomCode", roomCode);
        send(roomCode, payload);
    }

    private void send(String roomCode, Map<String, Object> payload) {
        List<SseEmitter> list = emittersByRoom.getOrDefault(roomCode, Collections.emptyList());
        List<SseEmitter> toRemove = new ArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("room-event")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                toRemove.add(emitter);
            }
        }
        toRemove.forEach(e -> remove(roomCode, e));
    }

    private void remove(String roomCode, SseEmitter emitter) {
        emittersByRoom.computeIfPresent(roomCode, (k, v) -> {
            v.remove(emitter);
            return v.isEmpty() ? null : v;
        });
    }
}
