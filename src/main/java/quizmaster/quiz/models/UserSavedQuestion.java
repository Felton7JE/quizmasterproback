package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_saved_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSavedQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String questionText;

    // Storing options as a simple JSON string or comma-separated if simple enough.
    // For simplicity, we use JSON array string.
    @Column(nullable = false, length = 1000)
    private String optionsJson;

    @Column(nullable = false)
    private Integer correctAnswer;

    @Column(length = 2000)
    private String explanation;

    @Column(length = 100)
    private String topic;

    @Column(length = 50)
    private String difficulty;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
