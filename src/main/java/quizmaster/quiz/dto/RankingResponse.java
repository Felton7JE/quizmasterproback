package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class RankingResponse {
    private Integer position;
    private Long userId;
    private String username;
    private String avatar;
    private Integer totalPoints;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Double accuracy;
    private Double winRate;
    private Integer streak;
    private Integer level;
    private Long activeTitleId;
    private String activeTitleName;
    private Long activeBannerId;
    private String activeBannerUrl;
    private Long activePhraseId;
    private Boolean isVip;
}