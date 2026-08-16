package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.models.Category;
import quizmaster.quiz.models.User;
import quizmaster.quiz.models.UserCategoryStats;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCategoryStatsRepository extends JpaRepository<UserCategoryStats, Long> {
    
    Optional<UserCategoryStats> findByUserAndCategory(User user, Category category);
    
    @Query("SELECT s FROM UserCategoryStats s WHERE s.category.name = :categoryName ORDER BY s.totalPoints DESC")
    List<UserCategoryStats> findTopByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT s FROM UserCategoryStats s WHERE s.category.name = :categoryName ORDER BY s.weeklyPoints DESC")
    List<UserCategoryStats> findTopWeeklyByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT s FROM UserCategoryStats s WHERE s.category.name = :categoryName ORDER BY s.monthlyPoints DESC")
    List<UserCategoryStats> findTopMonthlyByCategoryName(@Param("categoryName") String categoryName);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE UserCategoryStats s SET s.weeklyPoints = 0")
    void resetWeeklyPoints();

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE UserCategoryStats s SET s.monthlyPoints = 0")
    void resetMonthlyPoints();
}
