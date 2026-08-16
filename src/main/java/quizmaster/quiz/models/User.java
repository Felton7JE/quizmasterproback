package quizmaster.quiz.models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String email;
    
    private String avatar;
    private Integer totalPoints = 0;
    private Integer weeklyPoints = 0;
    private Integer monthlyPoints = 0;
    private Integer gamesPlayed = 0;
    private Integer gamesWon = 0;
    private Double accuracy = 0.0;
    private Integer bestStreak = 0;
    private Integer currentStreak = 0;
    private LocalDateTime lastPlayedDate;
    
    // Gamification & Economy
    private Integer coins = 500;
    private Integer crystals = 100; // Começa com 100 cristais mágicos grátis para IA e VIP
    private Integer energy = 5; // Starting energy (Max 5)
    private LocalDateTime lastEnergyUpdate;

    // AI Quiz Quota & Cooldown
    private Integer dailyAiQuizCount = 0;
    private java.time.LocalDate lastAiQuizDate;
    private LocalDateTime lastAiQuizTimestamp;

    // Referral System (5 amigos = 15 cristais)
    @Column(unique = true)
    private String referralCode;
    private Integer referralCount = 0;
    private Long referredById;

    
    private Integer xp = 0;
    private Integer level = 1;
    
    private Long activeTitleId;
    private Long activeBannerId;
    private Long activePhraseId;
    private Long activeAvatarId;
    private Long activeFrameId;
    private Long activeEmoteId;
    
    @Enumerated(EnumType.STRING)
    private quizmaster.quiz.enums.League currentLeague = quizmaster.quiz.enums.League.BRONZE;
    private Integer eloPoints = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @OneToMany(mappedBy = "user")
    private List<GameResult> gameResults;

    /**
     * Calcula o nível atual do jogador usando uma curva exponencial suave.
     * Fórmula: Nível = 1 + raiz(XP / 100)
     */
    public void updateLevelBasedOnXp() {
        if (this.xp == null) this.xp = 0;
        this.level = 1 + (int) Math.sqrt(this.xp / 100.0);
    }
}