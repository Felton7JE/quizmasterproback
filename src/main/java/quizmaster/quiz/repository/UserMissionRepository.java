package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.User;
import quizmaster.quiz.models.UserMission;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findByUser(User user);
    List<UserMission> findByUserAndMission_Type(User user, quizmaster.quiz.enums.MissionType type);
    List<UserMission> findByUserAndMission_TypeAndAssignedDateBetween(User user, quizmaster.quiz.enums.MissionType type, LocalDate startDate, LocalDate endDate);
    List<UserMission> findByUserAndMission_TypeAndAssignedDate(User user, quizmaster.quiz.enums.MissionType type, LocalDate assignedDate);
}
