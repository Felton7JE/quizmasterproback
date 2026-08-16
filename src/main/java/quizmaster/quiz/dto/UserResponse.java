package quizmaster.quiz.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer totalPoints;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Double accuracy;
    private Integer bestStreak;
    
    private Integer coins;
    private Integer crystals;
    private Integer energy;
    private Integer xp;
    private Integer level;
    private Long activeTitleId;
    private Long activeBannerId;
    private Long activePhraseId;
    private Long activeAvatarId;
    private Long activeFrameId;
    private Long activeEmoteId;
    private quizmaster.quiz.enums.League currentLeague;
    private Integer eloPoints;
    private Boolean isVip;
    private Integer dailyAiQuizCount;
    private LocalDateTime lastAiQuizTimestamp;
    private String referralCode;
    private Integer referralCount;
    
    private LocalDateTime createdAt;
    private String token;
}