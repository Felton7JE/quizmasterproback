package quizmaster.quiz.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.Team;

@Table(name = "room_players")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoomPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private Team team;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_category_id")
    private Category preferredCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_category_id")
    private Category assignedCategory;
    
    private Boolean isHost = false;
    private Boolean isReady = false;
    
    private LocalDateTime joinedAt;
}