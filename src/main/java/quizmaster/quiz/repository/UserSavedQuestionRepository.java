package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.UserSavedQuestion;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSavedQuestionRepository extends JpaRepository<UserSavedQuestion, Long> {
    List<UserSavedQuestion> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndQuestionText(Long userId, String questionText);
    Optional<UserSavedQuestion> findByIdAndUserId(Long id, Long userId);
}
