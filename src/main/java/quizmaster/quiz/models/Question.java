package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.Difficulty;

import java.util.List;
import quizmaster.quiz.models.Category;

@Table(name = "questions")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String questionText;
    
    @ElementCollection
    private List<String> options;
    
    private Integer correctAnswer;
    
    @Column(length = 2000)
    private String explanation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    private Integer points = 100;
}