package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class StudyPlanDayDto {
    private Long id;
    private Integer dayNumber;
    private String title;
    private String description;
    private Boolean isCompleted;
}
