package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryEntityRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find all active categories
     */
    List<Category> findByIsActiveTrueOrderByDisplayName();
    
    /**
     * Find category by name (case insensitive)
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(?1)")
    Optional<Category> findByNameIgnoreCase(String name);
    
    /**
     * Check if category exists by name (case insensitive)
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(?1)")
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Find category by display name (case insensitive)
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.displayName) = LOWER(?1)")
    Optional<Category> findByDisplayNameIgnoreCase(String displayName);
    
    /**
     * Find all categories (including inactive) ordered by name
     */
    List<Category> findAllByOrderByDisplayName();
    
    /**
     * Find categories by active status
     */
    List<Category> findByIsActiveOrderByDisplayName(Boolean isActive);
}
