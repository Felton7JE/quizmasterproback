package quizmaster.quiz.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.models.Room;
import quizmaster.quiz.models.RoomPlayer;

@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer, Long> {
    Optional<RoomPlayer> findByRoomAndUserId(Room room, Long userId);
    
    @Modifying
    @Query("DELETE FROM RoomPlayer rp WHERE rp.room = :room AND rp.user.id = :userId")
    void deleteByRoomAndUserId(Room room, Long userId);
}