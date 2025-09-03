package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private Long userId;
    private String username;
    private String message;
    private String roomCode;
    private Long timestamp;
}
