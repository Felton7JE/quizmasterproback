package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import quizmaster.quiz.models.GameQuestion;

import java.util.List;

public interface GameQuestionRepository extends JpaRepository<GameQuestion, Long> {
    @Query("SELECT gq FROM GameQuestion gq WHERE gq.game.id = :gameId ORDER BY gq.orderIndex ASC")
    List<GameQuestion> findByGameOrdered(Long gameId);
}
