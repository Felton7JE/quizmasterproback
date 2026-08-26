package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.RedemptionLedger;

@Repository
public interface RedemptionLedgerRepository extends JpaRepository<RedemptionLedger, Long> {
    boolean existsByPromoCodeIdAndUserId(Long promoCodeId, Long userId);
    long countByPromoCodeId(Long promoCodeId);
    long countByPromoCode_CampaignId(Long campaignId);
}
