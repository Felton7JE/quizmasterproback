package quizmaster.quiz.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SavedQuestionResponse {
    private Long id;
    private String questionText;
    private List<String> options;
    private Integer correctAnswer;
    private String explanation;
    private String topic;
    private String difficulty;
    private LocalDateTime createdAt;
}
