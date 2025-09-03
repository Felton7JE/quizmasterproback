package quizmaster.quiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.enums.Category;
import quizmaster.quiz.enums.Difficulty;
import quizmaster.quiz.models.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCategoryAndDifficulty(Category category, Difficulty difficulty);
    
    @Query(value = "SELECT * FROM questions WHERE category IN (:categories) AND difficulty = :difficulty ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestions(List<String> categories, String difficulty);
    
    List<Question> findByCategory(Category category);
    List<Question> findByDifficulty(Difficulty difficulty);
}