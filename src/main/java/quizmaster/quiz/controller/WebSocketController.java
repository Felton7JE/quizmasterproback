package quizmaster.quiz.controller;

import quizmaster.quiz.dto.*;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    
    @MessageMapping("/room/{roomCode}/join")
    @SendTo("/topic/room/{roomCode}")
    public PlayerJoinedMessage playerJoined(@DestinationVariable String roomCode, PlayerJoinedMessage message) {
        return message;
    }
    
    @MessageMapping("/room/{roomCode}/leave")
    @SendTo("/topic/room/{roomCode}")
    public PlayerLeftMessage playerLeft(@DestinationVariable String roomCode, PlayerLeftMessage message) {
        return message;
    }
    
    @MessageMapping("/room/{roomCode}/ready")
    @SendTo("/topic/room/{roomCode}")
    public PlayerReadyMessage playerReady(@DestinationVariable String roomCode, PlayerReadyMessage message) {
        return message;
    }
    
    @MessageMapping("/room/{roomCode}/team")
    @SendTo("/topic/room/{roomCode}")
    public TeamAssignedMessage teamAssigned(@DestinationVariable String roomCode, TeamAssignedMessage message) {
        return message;
    }
    
    @MessageMapping("/room/{roomCode}/start")
    @SendTo("/topic/room/{roomCode}")
    public GameStartedMessage gameStarted(@DestinationVariable String roomCode, GameStartedMessage message) {
        return message;
    }
    
    @MessageMapping("/game/{gameId}/answer")
    @SendTo("/topic/game/{gameId}")
    public PlayerAnsweredMessage playerAnswered(@DestinationVariable Long gameId, PlayerAnsweredMessage message) {
        return message;
    }
    
    @MessageMapping("/game/{gameId}/question")
    @SendTo("/topic/game/{gameId}")
    public QuestionStartedMessage questionStarted(@DestinationVariable Long gameId, QuestionStartedMessage message) {
        return message;
    }
    
    @MessageMapping("/game/{gameId}/leaderboard")
    @SendTo("/topic/game/{gameId}")
    public LeaderboardUpdateMessage leaderboardUpdate(@DestinationVariable Long gameId, LeaderboardUpdateMessage message) {
        return message;
    }
    
    @MessageMapping("/game/{gameId}/finish")
    @SendTo("/topic/game/{gameId}")
    public GameFinishedMessage gameFinished(@DestinationVariable Long gameId, GameFinishedMessage message) {
        return message;
    }
    
    @MessageMapping("/room/{roomCode}/chat")
    @SendTo("/topic/room/{roomCode}/chat")
    public ChatMessage chatMessage(@DestinationVariable String roomCode, ChatMessage message) {
        return message;
    }
    
    @MessageMapping("/room/{roomCode}/settings")
    @SendTo("/topic/room/{roomCode}")
    public RoomSettingsUpdatedMessage settingsUpdated(@DestinationVariable String roomCode, RoomSettingsUpdatedMessage message) {
        return message;
    }
}