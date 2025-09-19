package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import quizmaster.quiz.models.GameCategoryQuestion;

import java.util.List;

public interface GameCategoryQuestionRepository extends JpaRepository<GameCategoryQuestion, Long> {

    @Query("SELECT gcq FROM GameCategoryQuestion gcq WHERE gcq.game.id = :gameId AND gcq.category.id = :categoryId ORDER BY gcq.orderIndex ASC")
    List<GameCategoryQuestion> findByGameAndCategoryOrdered(Long gameId, Long categoryId);
}
