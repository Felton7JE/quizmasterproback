package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoloLevelDto {
    private Integer levelNumber;
    private String categoryName;
    private String categoryDisplayName;
    private String difficulty;
    private Boolean unlocked;
    private Boolean completed;
    private Integer starsCount; // 0..3
    private Integer highScore;
    private Boolean isBossLevel;
    private String bossName;
    private String bossAvatar;
    private Integer bossLivesRemaining; // 3 if normal level
    private Integer requiredStarsToUnlock; // New field for Star Gates
}
