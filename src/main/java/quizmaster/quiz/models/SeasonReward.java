package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.RewardType;

@Table(name = "season_rewards")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SeasonReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(nullable = false)
    private Integer levelRequired;

    @Enumerated(EnumType.STRING)
    private RewardType freeRewardType;

    private String freeRewardValue; // E.g., "50" for coins, or "title_1"

    @Enumerated(EnumType.STRING)
    private RewardType premiumRewardType;

    private String premiumRewardValue;

    private String freeRewardImageUrl;
    private String premiumRewardImageUrl;

    private Boolean isBossLevel = false;
    private String bossName;
    private String bossImageUrl;
}
