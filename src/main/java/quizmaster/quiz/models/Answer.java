package quizmaster.quiz.models;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
@Data
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
    
    private Integer selectedAnswer;
    private Boolean isCorrect;
    private Integer points;
    private Long timeToAnswer;
    
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}