package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.enums.FriendshipStatus;
import quizmaster.quiz.models.Friendship;
import quizmaster.quiz.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.user = :user1 AND f.friend = :user2) OR (f.user = :user2 AND f.friend = :user1)")
    Optional<Friendship> findFriendshipBetween(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT f FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = :status")
    List<Friendship> findAllByUserAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status);
    
    // Para encontrar apenas pedidos que o utilizador RECEBEU e estão pendentes
    List<Friendship> findByFriendAndStatus(User friend, FriendshipStatus status);
    
    // Para contar quantos amigos aceites o utilizador tem (limite de 30)
    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = 'ACCEPTED'")
    long countAcceptedFriends(@Param("user") User user);
}
