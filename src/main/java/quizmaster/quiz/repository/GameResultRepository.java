package quizmaster.quiz.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.models.Game;
import quizmaster.quiz.models.GameResult;
import quizmaster.quiz.models.User;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    List<GameResult> findByGameOrderByTotalPointsDesc(Game game);
    Optional<GameResult> findByGameAndUser(Game game, User user);
    List<GameResult> findByUserOrderByGame_StartedAtDesc(User user);
}