package quizmaster.quiz.auth.Servico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import quizmaster.quiz.auth.Entidade.UsuDetImp;
import quizmaster.quiz.auth.Entidade.cadastroEnt;
import quizmaster.quiz.auth.Repositorio.cadastroRep;

@Service
public class usuDetImpSer implements UserDetailsService {

    @Autowired
    private cadastroRep cadastroRep;

    @Autowired
    private quizmaster.quiz.repository.UserRepository userRepository;

    private cadastroEnt cadastroEnt;
    private quizmaster.quiz.models.User userEnt;
 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var optCadastro = cadastroRep.findByLogin(username);
        if (optCadastro.isPresent()) {
            this.cadastroEnt = optCadastro.get();
            return UsuDetImp.build(this.cadastroEnt);
        }
        
        var optUser = userRepository.findFirstByUsernameAndActiveTrue(username);
        if (optUser.isPresent()) {
            this.userEnt = optUser.get();
            return UsuDetImp.build(this.userEnt);
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    public Long getId() {
        if (this.cadastroEnt != null) return this.cadastroEnt.getId();
        if (this.userEnt != null) return this.userEnt.getId();
        return null;
    }

    public void UsuDetImp(cadastroEnt cadastroEnt) {
        this.cadastroEnt = cadastroEnt;
    }}

