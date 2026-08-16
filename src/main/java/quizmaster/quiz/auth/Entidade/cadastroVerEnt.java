package quizmaster.quiz.auth.Entidade;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tbl_cadastroVerificador")
public class cadastroVerEnt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 10)
    private String codigoAtivacao;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Instant dataExpericacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CADASTRO", referencedColumnName = "ID", unique = true, nullable = false)
    private cadastroEnt cadastroEnt;

}
