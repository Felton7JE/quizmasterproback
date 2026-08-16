package quizmaster.quiz.auth.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import quizmaster.quiz.auth.Entidade.cadastroVerEnt;
import java.util.Optional;
import java.util.UUID;


public interface cadastroVerRep extends JpaRepository<cadastroVerEnt, Long> {
    
    public Optional <cadastroVerEnt> findByUuid(UUID uuid);

    Optional<cadastroVerEnt> findByCodigoAtivacao(String codigoAtivacao);


}

