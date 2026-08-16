package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoloFinishLevelRequest {
    private Long userId;
    private Integer levelNumber;
    private Integer playerScore;
    private Integer botScore;
    private Integer correctCount;
    private Integer totalQuestions;
    private List<QuestionAnswerDto> answeredQuestions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswerDto {
        private Long questionId;
        private Boolean wasCorrect;
    }
}
