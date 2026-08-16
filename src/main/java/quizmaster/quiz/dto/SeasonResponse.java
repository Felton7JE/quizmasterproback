package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import quizmaster.quiz.models.Season;
import quizmaster.quiz.models.SeasonReward;
import quizmaster.quiz.models.UserSeasonProgress;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonResponse {
    private Long seasonId;
    private String name;
    private String description;
    private Long exclusiveCategoryId;
    private Integer currentLevel;
    private Integer seasonPoints;
    private boolean isPremium;
    private List<SeasonReward> rewards;
    private Integer lastClaimedFreeLevel;
    private Integer lastClaimedPremiumLevel;
    private String bannerUrl;
    private String mapBackgroundUrl;
    private String lockedNodeIconUrl;
    private String currentNodeIconUrl;
    private String completedNodeIconUrl;
}
