package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "user_question_history", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "question_id"})
})
@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserQuestionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();

    @Column(name = "was_correct", nullable = false)
    private Boolean wasCorrect = false;

    @Column(name = "consecutive_correct")
    private Integer consecutiveCorrect = 1;

    public UserQuestionHistory(User user, Question question, Boolean wasCorrect) {
        this.user = user;
        this.question = question;
        this.wasCorrect = wasCorrect;
        this.answeredAt = LocalDateTime.now();
        this.consecutiveCorrect = wasCorrect ? 1 : 0;
    }
}
