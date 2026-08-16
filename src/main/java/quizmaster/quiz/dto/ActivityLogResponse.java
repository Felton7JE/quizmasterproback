package quizmaster.quiz.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityLogResponse {
    private Long id;
    private String type;
    private String title;
    private String description;
    private String points;
    private LocalDateTime createdAt;
    private boolean isGlobal;
}
