package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private Long userId;
    private Long questionId;
    private Integer selectedAnswer;
    private Long timeToAnswer; // em millisegundos
}