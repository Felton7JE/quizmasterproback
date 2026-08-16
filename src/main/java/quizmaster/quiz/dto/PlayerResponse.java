package quizmaster.quiz.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.enums.Team;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PlayerResponse {
    private Long userId;
    private String username;
    private String avatar;
    private Team team;
    private Category preferredCategory;
    private Category assignedCategory;
    private Boolean isHost;
    private Boolean isReady;
    
    // Gamification & Cosmetics fields
    private Long activeTitleId;
    private Long activeBannerId;
    private Long activePhraseId;
    private Long activeAvatarId;
    private Long activeFrameId;
    private Long activeEmoteId;
    private Integer level;
    private Integer eloPoints;
    private Boolean isVip;
}