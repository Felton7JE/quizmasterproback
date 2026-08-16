package quizmaster.quiz.auth.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import quizmaster.quiz.auth.Entidade.cadastroEnt;

public interface cadastroRep extends JpaRepository<cadastroEnt, Long> {
    
    Optional<cadastroEnt> findByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByContato(Integer contato); 

}

