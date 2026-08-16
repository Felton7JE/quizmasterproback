package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyQuestionDto {
    private String id;
    private String questionText;
    private List<String> options;
    private Integer correctAnswer;
    private String explanation;
    private String hint;
    private String topic;
    private String difficulty;
}
