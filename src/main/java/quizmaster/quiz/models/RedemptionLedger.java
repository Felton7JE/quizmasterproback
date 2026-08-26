package quizmaster.quiz.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "redemption_ledger", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"promo_code_id", "user_id"})
})
@Data
@NoArgsConstructor
public class RedemptionLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id", nullable = false)
    private PromoCode promoCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String rewardType;

    @Column(nullable = false)
    private Integer rewardGranted;

    private LocalDateTime redeemedAt = LocalDateTime.now();
}
