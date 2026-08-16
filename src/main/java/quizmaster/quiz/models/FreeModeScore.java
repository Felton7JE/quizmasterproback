package quizmaster.quiz.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "free_mode_scores")
public class FreeModeScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "game_mode", nullable = false)
    private String gameMode;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "highest_streak", nullable = false)
    private int highestStreak;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FreeModeScore() {}

    public FreeModeScore(User user, String gameMode, int score, int highestStreak) {
        this.user = user;
        this.gameMode = gameMode;
        this.score = score;
        this.highestStreak = highestStreak;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public void setHighestStreak(int highestStreak) {
        this.highestStreak = highestStreak;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
