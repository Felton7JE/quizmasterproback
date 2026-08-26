package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_campaigns")
@Data
@NoArgsConstructor
public class PromoCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String rewardType; // CRYSTALS, COINS, VIP_PASS

    @Column(nullable = false)
    private Integer rewardAmount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer globalUsageLimit; // Optional limit for the entire campaign

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}
