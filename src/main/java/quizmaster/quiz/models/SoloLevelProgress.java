package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "solo_level_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "level_number"})
})
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SoloLevelProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "level_number", nullable = false)
    private Integer levelNumber;

    @Column(name = "stars_count", nullable = false)
    private Integer starsCount = 0; // 0, 1, 2, 3

    @Column(name = "high_score", nullable = false)
    private Integer highScore = 0;

    @Column(nullable = false)
    private Boolean unlocked = false;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "is_boss_level", nullable = false)
    private Boolean isBossLevel = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SoloLevelProgress(User user, Integer levelNumber, Boolean unlocked, Boolean isBossLevel) {
        this.user = user;
        this.levelNumber = levelNumber;
        this.unlocked = unlocked;
        this.isBossLevel = isBossLevel;
        this.completed = false;
        this.starsCount = 0;
        this.highScore = 0;
        this.updatedAt = LocalDateTime.now();
    }
}
