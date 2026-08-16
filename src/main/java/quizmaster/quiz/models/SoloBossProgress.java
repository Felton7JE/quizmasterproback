package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "solo_boss_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "boss_level_number"})
})
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SoloBossProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "boss_level_number", nullable = false)
    private Integer bossLevelNumber;

    @Column(name = "lives_remaining", nullable = false)
    private Integer livesRemaining = 3;

    @Column(name = "defeated", nullable = false)
    private Boolean defeated = false;

    @Column(name = "attempts_count", nullable = false)
    private Integer attemptsCount = 0;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    public SoloBossProgress(User user, Integer bossLevelNumber) {
        this.user = user;
        this.bossLevelNumber = bossLevelNumber;
        this.livesRemaining = 3;
        this.defeated = false;
        this.attemptsCount = 0;
        this.lastAttemptAt = LocalDateTime.now();
    }
}
