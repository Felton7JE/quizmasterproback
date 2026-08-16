package quizmaster.quiz.auth.DTO;

import org.springframework.beans.BeanUtils;
import lombok.Data;
import quizmaster.quiz.auth.Entidade.cadastroEnt;
import quizmaster.quiz.auth.Entidade.enums.contaPrevilegio;
import quizmaster.quiz.auth.Entidade.enums.estadoCadastro;

@Data
public class cadastroDTO { 

        private Long id;

        private Integer contato; 

        private String nome;
    
        private String email;
        

        private String senha; 

        private estadoCadastro situacao;

       private contaPrevilegio contaPrevilegio;


         public cadastroDTO(cadastroEnt cadastroEnt){
                
                BeanUtils.copyProperties(cadastroEnt, this);

        }

        public cadastroDTO(){

        }

}

