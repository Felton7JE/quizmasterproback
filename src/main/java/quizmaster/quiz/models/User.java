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
    private Integer gamesPlayed = 0;
    private Integer gamesWon = 0;
    private Double accuracy = 0.0;
    private Integer bestStreak = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user")
    private List<GameResult> gameResults;
}