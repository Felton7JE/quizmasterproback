package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.Room;
import quizmaster.quiz.enums.RoomStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Optional<Room> findByRoomCode(String roomCode);
    
    List<Room> findByStatus(RoomStatus status);
    
    // ✅ CORREÇÃO: Use query customizada para buscar por host.id
    @Query("SELECT r FROM Room r WHERE r.host.id = :hostId")
    List<Room> findByHostId(@Param("hostId") Long hostId);



}  