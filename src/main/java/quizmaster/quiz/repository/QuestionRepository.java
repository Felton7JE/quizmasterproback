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
    
    // Ajustado: a coluna antiga 'category' foi migrada para relacionamento ManyToOne (category_id)
    // Agora fazemos JOIN com 'categories' usando o campo name para filtrar.
    @Query(value = "SELECT q.* FROM questions q JOIN categories c ON q.category_id = c.id " +
        "WHERE c.name IN (:categories) AND q.difficulty = :difficulty ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestions(List<String> categories, String difficulty);

    @Query(value = "SELECT q.* FROM questions q WHERE q.category_id = ?1 AND q.difficulty = ?2 ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomByCategory(Long categoryId, String difficulty);
    
    List<Question> findByCategory(Category category);
    List<Question> findByDifficulty(Difficulty difficulty);
}