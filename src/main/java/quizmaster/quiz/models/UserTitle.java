package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_titles")
@Getter
@Setter
@NoArgsConstructor
public class UserTitle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    private Boolean isEquipped = false;
    
    @PrePersist
    protected void onCreate() {
        unlockedAt = LocalDateTime.now();
    }
}
