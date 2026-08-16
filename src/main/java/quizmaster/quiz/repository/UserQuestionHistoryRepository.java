package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import quizmaster.quiz.models.Question;
import quizmaster.quiz.models.UserQuestionHistory;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserQuestionHistoryRepository extends JpaRepository<UserQuestionHistory, Long> {

    Optional<UserQuestionHistory> findByUserIdAndQuestionId(Long userId, Long questionId);

    @Query("SELECT uqh.question.id FROM UserQuestionHistory uqh WHERE uqh.user.id = :userId")
    List<Long> findAnsweredQuestionIdsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM questions q WHERE q.category_id = :categoryId AND q.difficulty = :difficulty AND q.id NOT IN (SELECT uqh.question_id FROM user_question_history uqh WHERE uqh.user_id = :userId) ORDER BY RAND()", nativeQuery = true)
    List<Question> findUnseenByCategoryAndDifficulty(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("difficulty") String difficulty, Pageable pageable);

    @Query(value = "SELECT * FROM questions q WHERE q.category_id = :categoryId AND q.id NOT IN (SELECT uqh.question_id FROM user_question_history uqh WHERE uqh.user_id = :userId) ORDER BY RAND()", nativeQuery = true)
    List<Question> findUnseenByCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM questions q WHERE q.id NOT IN (SELECT uqh.question_id FROM user_question_history uqh WHERE uqh.user_id = :userId) ORDER BY RAND()", nativeQuery = true)
    List<Question> findUnseenRandom(@Param("userId") Long userId, Pageable pageable);

    void deleteByUserIdAndQuestion_Category_Id(Long userId, Long categoryId);
}
