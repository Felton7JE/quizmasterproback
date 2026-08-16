package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.Mission;
import quizmaster.quiz.enums.MissionType;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByType(MissionType type);
}
