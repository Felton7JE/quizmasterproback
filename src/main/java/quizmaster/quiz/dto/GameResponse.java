package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class GameResponse {
    private Long gameId; // ID do jogo
    private String roomCode;
    private String status;
    private Integer currentQuestionIndex;
    private Integer totalQuestions;
    private String gameMode;
    private String category;
    private String difficulty;
    private Long startTime;
    private Long endTime;
}
