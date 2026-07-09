package quizmaster.quiz.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import quizmaster.quiz.models.Game;
import quizmaster.quiz.models.GameQuestion;
import quizmaster.quiz.enums.GameStatus;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class GameQuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameQuestionRepository repository;

    @Test
    public void shouldReturnQuestionsOrderedByIndex() {
        // Arrange
        Game game = new Game();
        game.setStatus(GameStatus.IN_PROGRESS);
        game = entityManager.persist(game);

        GameQuestion q1 = new GameQuestion();
        q1.setGame(game);
        q1.setOrderIndex(3); // Inserindo fora de ordem propositalmente
        entityManager.persist(q1);

        GameQuestion q2 = new GameQuestion();
        q2.setGame(game);
        q2.setOrderIndex(1);
        entityManager.persist(q2);

        GameQuestion q3 = new GameQuestion();
        q3.setGame(game);
        q3.setOrderIndex(2);
        entityManager.persist(q3);

        entityManager.flush();

        // Act
        List<GameQuestion> result = repository.findByGameOrdered(game.getId());

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(result.get(1).getOrderIndex()).isEqualTo(2);
        assertThat(result.get(2).getOrderIndex()).isEqualTo(3);
    }
}
