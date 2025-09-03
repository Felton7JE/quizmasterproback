package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class AnswerResponse {
    private Boolean isCorrect;
    private Integer correctAnswer;
    private String explanation;
    private Integer points;
    private Integer currentStreak;
    private Integer totalPoints;
}