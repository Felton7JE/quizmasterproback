package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class GameStatsResponse {
    private Long gameId;
    private Integer totalPlayers;
    private Integer questionsAnswered;
    private Integer totalQuestions;
    private Double averageScore;
    private Long duration;
}
