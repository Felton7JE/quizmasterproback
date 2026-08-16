package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoloStartLevelResponse {
    private Integer levelNumber;
    private String categoryName;
    private String difficulty;
    private Boolean isBossLevel;
    
    // Bot Information
    private String botName;
    private String botAvatar;
    private Double botAccuracyRate;
    private Integer botMinDelayMs;
    private Integer botMaxDelayMs;
    private String bossTaunt;

    // Questions (5 questions)
    private List<QuestionResponse> questions;
}
