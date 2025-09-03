package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class PlayerJoinedMessage {
    private Long userId;
    private String username;
    private String roomCode;
    private Long timestamp;
}
