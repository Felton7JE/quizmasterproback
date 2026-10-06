package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.StudyPlanDay;
import java.util.List;

@Repository
public interface StudyPlanDayRepository extends JpaRepository<StudyPlanDay, Long> {
    List<StudyPlanDay> findByStudyPlanIdOrderByDayNumberAsc(Long studyPlanId);
}
