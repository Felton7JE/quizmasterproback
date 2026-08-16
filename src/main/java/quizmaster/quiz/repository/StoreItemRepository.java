package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.StoreItem;
import quizmaster.quiz.enums.ItemType;
import java.util.List;

import java.util.Optional;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {
    List<StoreItem> findByType(ItemType type);
    Optional<StoreItem> findByValue(String value);
    Optional<StoreItem> findByName(String name);
    Optional<StoreItem> findFirstByValue(String value);
    Optional<StoreItem> findFirstByName(String name);
}

