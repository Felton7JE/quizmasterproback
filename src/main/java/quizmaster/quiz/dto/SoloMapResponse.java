package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoloMapResponse {
    private Long userId;
    private Integer currentUnlockedLevel;
    private Integer totalStars;
    private Integer currentEnergy;
    private Long secondsUntilNextEnergy;
    private List<SoloLevelDto> levels;
}
