package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.Team;

@Table(name = "game_results")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class GameResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private Integer correctAnswers = 0;
    private Integer totalQuestions = 0;
    private Integer totalPoints = 0;
    private Double accuracy = 0.0;
    private Integer bestStreak = 0;
    private Long totalTime;
    
    @Enumerated(EnumType.STRING)
    private Team team;
    
    private Integer position;
    
    private Integer coinsEarned = 0;
    private Integer xpEarned = 0;
    private Integer eloEarned = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_result_category_points", joinColumns = @JoinColumn(name = "game_result_id"))
    @MapKeyJoinColumn(name = "category_id")
    @Column(name = "points")
    private java.util.Map<Category, Integer> categoryPoints = new java.util.HashMap<>();
}