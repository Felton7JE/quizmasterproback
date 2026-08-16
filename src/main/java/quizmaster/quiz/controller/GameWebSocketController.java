package quizmaster.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import quizmaster.quiz.dto.GameEventMessage;

import java.util.Map;

/**
 * Controlador STOMP para eventos de jogo em tempo real.
 *
 * Clientes Flutter ligam-se ao endpoint "/ws" (STOMP) e subscrevem:
 *   - /topic/room/{roomCode}  — eventos da sala (GAME_STARTED, ROOM_UPDATED, CHAT_MESSAGE, etc.)
 *   - /topic/game/{gameId}    — eventos do jogo (NEXT_QUESTION, GAME_ENDED, etc.)
 *
 * O servidor publica nesses tópicos via SimpMessagingTemplate.
 */
@Controller
@RequiredArgsConstructor
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Permite que um cliente envie uma mensagem de "ping" para confirmar ligação.
     * Mensagem enviada para /app/room/{roomCode}/ping
     * Resposta publicada em /topic/room/{roomCode}
     */
    @MessageMapping("/room/{roomCode}/ping")
    public void handlePing(@DestinationVariable String roomCode) {
        GameEventMessage pong = new GameEventMessage();
        pong.setType("PONG");
        pong.setRoomCode(roomCode);
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, pong);
    }

    /**
     * Permite que os jogadores enviem frases de provocação / reações / chat em tempo real durante a partida.
     * Mensagem enviada para /app/room/{roomCode}/chat
     * Transmissão enviada para todos os jogadores inscritos em /topic/room/{roomCode}
     */
    @MessageMapping("/room/{roomCode}/chat")
    public void handleChatMessage(@DestinationVariable String roomCode, Map<String, Object> messagePayload) {
        if (messagePayload == null) {
            return;
        }
        messagePayload.putIfAbsent("type", "CHAT_MESSAGE");
        messagePayload.put("roomCode", roomCode);
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, messagePayload);
    }
}
