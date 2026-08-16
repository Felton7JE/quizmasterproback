package quizmaster.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quizmaster.quiz.models.Title;

import java.util.Optional;

@Repository
public interface TitleRepository extends JpaRepository<Title, Long> {
    Optional<Title> findByName(String name);
    Optional<Title> findFirstByName(String name);
}
