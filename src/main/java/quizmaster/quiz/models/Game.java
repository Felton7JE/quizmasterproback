package quizmaster.quiz.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "games")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    
    @Enumerated(EnumType.STRING)
    private GameStatus status;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<GameResult> results;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Answer> answers;
}