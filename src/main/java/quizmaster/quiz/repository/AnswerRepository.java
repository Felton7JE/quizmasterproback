package quizmaster.quiz.repository;


import java.util.List;

import org.aspectj.weaver.patterns.TypePatternQuestions.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.models.Answer;
import quizmaster.quiz.models.Game;
import quizmaster.quiz.models.User;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByGameAndUser(Game game, User user);
    List<Answer> findByGame(Game game);
    boolean existsByGameAndUserAndQuestion(Game game, User user, quizmaster.quiz.models.Question question);
}