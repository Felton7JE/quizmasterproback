package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class CreateStudyPlanRequest {
    private Long userId;
    private String topic;
    private int durationDays;
    private String objective;
}
