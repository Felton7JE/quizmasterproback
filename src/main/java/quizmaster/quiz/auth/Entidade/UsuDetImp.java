package quizmaster.quiz.auth.Entidade;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import quizmaster.quiz.auth.Entidade.enums.estadoCadastro;

public class UsuDetImp implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private estadoCadastro situacao;
    private Collection<? extends GrantedAuthority> authorities;

    public UsuDetImp(Long id, String nome, String username, String password, String email, estadoCadastro situacao,
                     Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.situacao = situacao;
        this.authorities = authorities;
    }

    public static UsuDetImp build(cadastroEnt cadastroEnt) {
        return new UsuDetImp(
                cadastroEnt.getId(),
                cadastroEnt.getNome(),
                cadastroEnt.getLogin(),
                cadastroEnt.getSenha(),
                cadastroEnt.getEmail(),
                cadastroEnt.getSituacao(),  // Aqui estamos passando o estado correto
                new ArrayList<>()); // Inicializa authorities como uma lista vazia
    }

    public static UsuDetImp build(quizmaster.quiz.models.User user) {
        return new UsuDetImp(
                user.getId(),
                user.getUsername(),
                user.getUsername(),
                "", // Senha vazia para login do Google
                user.getEmail(),
                user.isActive() ? estadoCadastro.ACTIVO : estadoCadastro.INATIVO,
                new ArrayList<>());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.getSituacao() == estadoCadastro.ACTIVO;
    }

    public estadoCadastro getSituacao() {
        return situacao;
    }

    // Adiciona o método getId para retornar o ID do usuário
    public Long getId() {
        return id;
    }
}

