package quizmaster.quiz.dto;

import lombok.Data;
import java.util.Map;

@Data
public class UserStatsResponse {
    private Long userId;
    private String username;
    private Integer totalPoints;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Double winRate;
    private Double accuracy;
    private Integer bestStreak;
    private Integer currentStreak;
    private Map<String, Integer> categoryStats;
    private Integer globalRanking;
    private Integer weeklyRanking;

    // Free Mode Stats
    private Integer survivalHighScore;
    private Integer survivalBestStreak;
    private Integer timeAttackHighScore;

    // Season Stats
    private Integer seasonLevel;
    private Integer seasonPoints;
    private Boolean hasPremiumPass;
}