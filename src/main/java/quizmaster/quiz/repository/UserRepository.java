package quizmaster.quiz.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndActiveTrue(String username);
    Optional<User> findFirstByUsernameAndActiveTrue(String username);
    Optional<User> findFirstByUsername(String username);
    
    boolean existsByUsername(String username);
    
    Optional<User> findByReferralCode(String referralCode);
    boolean existsByReferralCode(String referralCode);
    
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.totalPoints DESC")
    List<User> findAllOrderByTotalPointsDesc();

    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.weeklyPoints DESC")
    List<User> findAllOrderByWeeklyPointsDesc();

    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.monthlyPoints DESC")
    List<User> findAllOrderByMonthlyPointsDesc();

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE User u SET u.weeklyPoints = 0")
    void resetWeeklyPoints();

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE User u SET u.monthlyPoints = 0")
    void resetMonthlyPoints();

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE User u SET u.energy = u.energy + 10 WHERE u.energy < 100")
    void regenerateEnergy();
}