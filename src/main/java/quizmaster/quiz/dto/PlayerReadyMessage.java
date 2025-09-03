package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class PlayerReadyMessage {
    private Long userId;
    private String username;
    private Boolean isReady;
    private String roomCode;
    private Long timestamp;
}
