package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "bug_reports")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class BugReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Can be null if anonymous
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
