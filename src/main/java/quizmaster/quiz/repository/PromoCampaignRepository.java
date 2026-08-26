package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.PromoCampaign;

@Repository
public interface PromoCampaignRepository extends JpaRepository<PromoCampaign, Long> {
}
