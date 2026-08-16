package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateStudyQuizRequest {
    private String title;
    private String content;
    private String topic;
    private String difficulty; // FACIL, MEDIO, DIFICIL, UNIVERSITARIO
    private Integer questionCount; // 5, 10, 15, 20
    private Long userId;
    private String sourceFileName;
    private String sourceType; // PDF, TEXT, TOPIC
}
