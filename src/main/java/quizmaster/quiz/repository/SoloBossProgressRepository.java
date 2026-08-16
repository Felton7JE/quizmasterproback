package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.SoloBossProgress;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoloBossProgressRepository extends JpaRepository<SoloBossProgress, Long> {

    Optional<SoloBossProgress> findByUserIdAndBossLevelNumber(Long userId, Integer bossLevelNumber);

    List<SoloBossProgress> findByUserId(Long userId);
}
