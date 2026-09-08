package quizmaster.quiz.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    // Map simpSessionId -> userId
    private final Map<String, Long> sessionToUserIdMap = new ConcurrentHashMap<>();
    
    // Conjunto de utilizadores online (usamos ConcurrentHashMap como Set)
    private final Set<Long> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        // Esperamos que o Flutter envie o userId nos headers nativos do STOMP ao conectar
        String userIdStr = accessor.getFirstNativeHeader("userId");
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                sessionToUserIdMap.put(sessionId, userId);
                onlineUsers.add(userId);
                
                // Avisar os amigos deste utilizador que ele ficou online
                broadcastStatusChange(userId, true);
                
                System.out.println("User " + userId + " connected (Session: " + sessionId + ")");
            } catch (NumberFormatException ignored) {}
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        Long userId = sessionToUserIdMap.remove(sessionId);
        if (userId != null) {
            // Verifica se o user ainda tem outras sessões abertas (por exemplo, múltiplas abas no browser)
            boolean hasOtherSessions = sessionToUserIdMap.containsValue(userId);
            if (!hasOtherSessions) {
                onlineUsers.remove(userId);
                // Avisar os amigos deste utilizador que ele ficou offline
                broadcastStatusChange(userId, false);
                System.out.println("User " + userId + " disconnected.");
            }
        }
    }

    public boolean isUserOnline(Long userId) {
        return onlineUsers.contains(userId);
    }
    
    public Set<Long> getOnlineUsers(Set<Long> userIdsToCheck) {
        return userIdsToCheck.stream()
                .filter(onlineUsers::contains)
                .collect(Collectors.toSet());
    }
    
    private void broadcastStatusChange(Long userId, boolean isOnline) {
        // Envia uma mensagem para o tópico global de status deste utilizador
        // Quem for amigo deste utilizador estará a ouvir em /topic/friends/{userId}/status
        String destination = "/topic/friends/" + userId + "/status";
        messagingTemplate.convertAndSend(destination, Map.of(
                "userId", userId,
                "isOnline", isOnline
        ));
    }
}
