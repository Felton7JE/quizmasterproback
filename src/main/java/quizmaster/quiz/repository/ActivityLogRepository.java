package quizmaster.quiz.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import quizmaster.quiz.models.ActivityLog;
import quizmaster.quiz.models.User;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    // Devolve as atividades globais e as do utilizador
    @Query("SELECT a FROM ActivityLog a WHERE a.user = :user OR a.user IS NULL ORDER BY a.createdAt DESC")
    Page<ActivityLog> findFeedForUser(@Param("user") User user, Pageable pageable);
    
    List<ActivityLog> findByUserOrderByCreatedAtDesc(User user);
}
