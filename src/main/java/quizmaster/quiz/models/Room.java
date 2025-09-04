package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.AssignmentType;
import quizmaster.quiz.enums.CategoryAssignmentMode;
import quizmaster.quiz.enums.Difficulty;
import quizmaster.quiz.enums.GameMode;
import quizmaster.quiz.enums.RoomStatus;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "rooms")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String roomCode;
    
    @Column(nullable = false)
    private String roomName;
    
    private String password;
    
    @Enumerated(EnumType.STRING)
    private GameMode gameMode;
    
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    private Integer maxPlayers;
    private Integer questionTime;
    private Integer questionCount;
    
    @Enumerated(EnumType.STRING)
    private AssignmentType assignmentType;
    
    @Enumerated(EnumType.STRING)
    private CategoryAssignmentMode categoryAssignmentMode;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "room_categories",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;
    
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;
    
    // Método getter
    public Long getHostId() {
        return host != null ? host.getId() : null;
    }
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoomPlayer> players;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Game> games;
    

    private LocalDateTime startsAt;


    // Settings
    private Boolean allowSpectators = true;
    private Boolean enableChat = true;
    private Boolean showRealTimeRanking = true;
    private Boolean allowReconnection = true;
}