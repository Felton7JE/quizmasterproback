package quizmaster.quiz.dto;

import lombok.Data;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.enums.Difficulty;

import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private String questionText;
    private List<String> options;
    private Category category;
    private Difficulty difficulty;
    private Integer points;
    private Integer timeLimit;
    // Não incluir correctAnswer na response!
}