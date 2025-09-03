package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class GameFinishedMessage {
    private Long gameId;
    private String roomCode;
    private GameResultResponse results;
    private Long timestamp;
}
