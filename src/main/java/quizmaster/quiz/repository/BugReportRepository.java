package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.BugReport;

@Repository
public interface BugReportRepository extends JpaRepository<BugReport, Long> {
}
