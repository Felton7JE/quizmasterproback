package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class QuestionStatsResponse {
    private Long totalQuestions;
    private Long questionsPerCategory;
    private Long questionsPerDifficulty;
    private Double averageCorrectRate;
}
