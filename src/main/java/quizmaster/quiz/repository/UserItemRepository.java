package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.UserItem;
import quizmaster.quiz.models.User;
import quizmaster.quiz.enums.ItemType;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    List<UserItem> findByUser(User user);
    List<UserItem> findByUserAndStoreItem_Type(User user, ItemType type);
    boolean existsByUserAndStoreItem_Id(User user, Long storeItemId);
    Optional<UserItem> findByUserAndStoreItem_Id(User user, Long storeItemId);
}
