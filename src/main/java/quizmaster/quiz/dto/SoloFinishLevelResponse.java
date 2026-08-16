package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoloFinishLevelResponse {
    private Boolean victory;
    private Integer levelNumber;
    private Integer starsEarned; // 0..3
    private Integer playerScore;
    private Integer botScore;
    private Integer xpEarned;
    private Integer coinsEarned;
    
    // Boss / Lives state
    private Boolean isBossLevel;
    private Integer bossLivesRemaining;
    private Boolean checkpointReverted;
    private Integer newCurrentLevel;
    private String message;
}
