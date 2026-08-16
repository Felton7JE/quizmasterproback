package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.SeasonReward;

import java.util.List;

@Repository
public interface SeasonRewardRepository extends JpaRepository<SeasonReward, Long> {
    List<SeasonReward> findBySeasonIdOrderByLevelRequiredAsc(Long seasonId);
    SeasonReward findBySeasonIdAndLevelRequired(Long seasonId, int levelRequired);
    SeasonReward findFirstBySeasonIdAndLevelRequired(Long seasonId, int levelRequired);
}
