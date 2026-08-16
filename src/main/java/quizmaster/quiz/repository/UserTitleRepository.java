package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.UserTitle;
import quizmaster.quiz.models.User;
import java.util.List;

@Repository
public interface UserTitleRepository extends JpaRepository<UserTitle, Long> {
    List<UserTitle> findByUser(User user);
    boolean existsByUserAndTitle_Id(User user, Long titleId);
}
