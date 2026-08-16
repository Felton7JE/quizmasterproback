package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class PlayerResultResponse {
    private Long userId;
    private String username;
    private String avatar;
    private String team;
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer totalPoints;
    private Double accuracy;
    private Integer bestStreak;
    private Long totalTime;
    private Integer position;
    private Integer coinsEarned;
    private Integer xpEarned;
    private Integer eloEarned;
    private Long activeBannerId;
    private Long activeAvatarId;
    private Long activeFrameId;
    private Long activePhraseId;
    private Boolean isVip;
}