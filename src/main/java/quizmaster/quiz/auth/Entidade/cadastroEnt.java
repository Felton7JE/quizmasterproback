package quizmaster.quiz.auth.Entidade;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.auth.DTO.cadastroDTO;
import quizmaster.quiz.auth.Entidade.enums.contaPrevilegio;
import quizmaster.quiz.auth.Entidade.enums.estadoCadastro;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "e_cadastro")
public class cadastroEnt {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String nome;

        @Column(nullable = true)
        private Integer contato;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        private String senha;

        @Column(nullable = false, unique = true)
        private String login;

        // estado do cadastro
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private estadoCadastro situacao;

        public cadastroEnt(cadastroDTO cadastroDTO) {

                BeanUtils.copyProperties(cadastroDTO, this);

        }

        public cadastroEnt(Optional<cadastroEnt> byLogin) {
                //TODO Auto-generated constructor stub
        }



        // tipo de conta
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private contaPrevilegio contaPrevilegio;

}

