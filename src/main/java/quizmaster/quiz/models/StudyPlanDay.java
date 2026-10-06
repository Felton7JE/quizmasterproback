package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_plan_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @Column(nullable = false)
    private Integer dayNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Boolean isCompleted;
    
    @PrePersist
    protected void onCreate() {
        if (isCompleted == null) isCompleted = false;
    }
}
