package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.SoloLevelProgress;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoloLevelProgressRepository extends JpaRepository<SoloLevelProgress, Long> {

    List<SoloLevelProgress> findByUserIdOrderByLevelNumberAsc(Long userId);

    Optional<SoloLevelProgress> findByUserIdAndLevelNumber(Long userId, Integer levelNumber);

    @Query("SELECT MAX(s.levelNumber) FROM SoloLevelProgress s WHERE s.user.id = :userId AND s.unlocked = true")
    Optional<Integer> findMaxUnlockedLevelNumberByUserId(@Param("userId") Long userId);
}
