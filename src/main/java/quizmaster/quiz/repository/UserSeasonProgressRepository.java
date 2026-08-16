package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.UserSeasonProgress;

import java.util.Optional;

@Repository
public interface UserSeasonProgressRepository extends JpaRepository<UserSeasonProgress, Long> {
    Optional<UserSeasonProgress> findByUserIdAndSeasonId(Long userId, Long seasonId);
    boolean existsByUserIdAndSeason_ActiveTrueAndIsPremiumPassTrue(Long userId);
}
