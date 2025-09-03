package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class GameStartedMessage {
    private String roomCode;
    private Long gameId;
    private Long timestamp;
    private Integer totalQuestions;
}
