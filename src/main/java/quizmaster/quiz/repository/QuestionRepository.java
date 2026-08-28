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

    @Query(value = "SELECT q.* FROM questions q WHERE q.id NOT IN :seenIds ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomUnseenQuestions(@org.springframework.data.repository.query.Param("seenIds") List<Long> seenIds, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT q.* FROM questions q ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestionsWithLimit(org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT q.* FROM questions q JOIN categories c ON q.category_id = c.id " +
        "WHERE c.name IN (:categories) AND q.difficulty IN (:difficulties) AND q.id NOT IN (:seenIds) ORDER BY RAND()", nativeQuery = true)
    List<Question> findUnseenRandomQuestionsByDiff(@org.springframework.data.repository.query.Param("categories") List<String> categories, @org.springframework.data.repository.query.Param("difficulties") List<String> difficulties, @org.springframework.data.repository.query.Param("seenIds") List<Long> seenIds, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT q.* FROM questions q JOIN categories c ON q.category_id = c.id " +
        "WHERE c.name IN (:categories) AND q.difficulty IN (:difficulties) ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestionsByDiff(@org.springframework.data.repository.query.Param("categories") List<String> categories, @org.springframework.data.repository.query.Param("difficulties") List<String> difficulties, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT q.* FROM questions q WHERE q.category_id = :categoryId AND q.difficulty IN (:difficulties) AND q.id NOT IN (:seenIds) ORDER BY RAND()", nativeQuery = true)
    List<Question> findUnseenByCategoryAndDiff(@org.springframework.data.repository.query.Param("categoryId") Long categoryId, @org.springframework.data.repository.query.Param("difficulties") List<String> difficulties, @org.springframework.data.repository.query.Param("seenIds") List<Long> seenIds, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT q.* FROM questions q WHERE q.category_id = :categoryId AND q.difficulty IN (:difficulties) ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomByCategoryAndDiff(@org.springframework.data.repository.query.Param("categoryId") Long categoryId, @org.springframework.data.repository.query.Param("difficulties") List<String> difficulties, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT q.* FROM questions q WHERE q.difficulty IN (:difficulties) AND q.id NOT IN (:seenIds) ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomUnseenQuestionsWithDifficulty(@org.springframework.data.repository.query.Param("seenIds") List<Long> seenIds, @org.springframework.data.repository.query.Param("difficulties") List<String> difficulties, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT q.* FROM questions q WHERE q.difficulty IN (:difficulties) ORDER BY RAND()", nativeQuery = true)
    List<Question> findRandomQuestionsWithLimitAndDifficulty(@org.springframework.data.repository.query.Param("difficulties") List<String> difficulties, org.springframework.data.domain.Pageable pageable);
}