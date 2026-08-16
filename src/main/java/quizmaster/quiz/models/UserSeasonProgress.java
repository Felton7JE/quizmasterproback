package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "user_season_progress")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserSeasonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    private Integer seasonPoints = 0;
    
    private Integer currentLevel = 1;

    private boolean isPremiumPass = false;

    private Integer lastClaimedFreeLevel = 0;
    
    private Integer lastClaimedPremiumLevel = 0;
}
