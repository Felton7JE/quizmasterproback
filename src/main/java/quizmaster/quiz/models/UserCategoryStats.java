package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_category_stats", indexes = {
    @Index(name = "idx_category_points", columnList = "category_id, total_points DESC"),
    @Index(name = "idx_category_weekly_points", columnList = "category_id, weekly_points DESC"),
    @Index(name = "idx_category_monthly_points", columnList = "category_id, monthly_points DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class UserCategoryStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "weekly_points")
    private Integer weeklyPoints = 0;

    @Column(name = "monthly_points")
    private Integer monthlyPoints = 0;
}
