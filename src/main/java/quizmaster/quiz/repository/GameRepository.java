package quizmaster.quiz.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.enums.GameStatus;
import quizmaster.quiz.models.Game;
import quizmaster.quiz.models.Room;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByRoomAndStatus(Room room, GameStatus status);
}