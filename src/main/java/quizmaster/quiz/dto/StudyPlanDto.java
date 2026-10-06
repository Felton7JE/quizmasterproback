package quizmaster.quiz.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudyPlanDto {
    private Long id;
    private String topic;
    private Integer durationDays;
    private String objective;
    private Integer progressPercentage;
    private LocalDateTime createdAt;
    private List<StudyPlanDayDto> days;
}
