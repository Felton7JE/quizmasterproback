package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class CreateQuestionRequest {
    private String text;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String correctAnswer;
    private String category;
    private String difficulty;
    private Integer timeLimit;
}
