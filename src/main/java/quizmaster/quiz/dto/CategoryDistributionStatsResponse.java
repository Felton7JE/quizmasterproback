package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.models.Category;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionStatsResponse {
    private Map<Category, CategoryTeamStats> distribution;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTeamStats {
        private Integer redPlayers;
        private Integer bluePlayers;
        private Integer totalPlayers;
    }
}
