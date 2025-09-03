package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class PlayerAnswerResponse {
    private Long questionId;
    private String question;
    private String playerAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer timeToAnswer;
    private Integer pointsEarned;
}
