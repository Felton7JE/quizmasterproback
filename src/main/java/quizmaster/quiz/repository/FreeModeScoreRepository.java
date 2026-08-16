package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.FreeModeScore;
import quizmaster.quiz.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FreeModeScoreRepository extends JpaRepository<FreeModeScore, Long> {
    
    Optional<FreeModeScore> findByUserAndGameMode(User user, String gameMode);
    
    List<FreeModeScore> findTop100ByGameModeOrderByScoreDesc(String gameMode);

    Optional<FreeModeScore> findFirstByGameModeAndScoreGreaterThanOrderByScoreAsc(String gameMode, int score);
}
