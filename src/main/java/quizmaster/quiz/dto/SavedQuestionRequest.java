package quizmaster.quiz.dto;

import lombok.Data;
import java.util.List;

@Data
public class SavedQuestionRequest {
    private Long userId;
    private String questionText;
    private List<String> options;
    private Integer correctAnswer;
    private String explanation;
    private String topic;
    private String difficulty;
}
