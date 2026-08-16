package quizmaster.quiz.auth.Servico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import quizmaster.quiz.auth.DTO.acessDTO;
import quizmaster.quiz.auth.DTO.authDTO;
import quizmaster.quiz.auth.Entidade.UsuDetImp;
import quizmaster.quiz.auth.Seguranca.jwt.utilJwt;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import quizmaster.quiz.auth.Repositorio.cadastroRep;
import quizmaster.quiz.auth.Entidade.cadastroEnt;
import quizmaster.quiz.auth.Entidade.enums.estadoCadastro;
import quizmaster.quiz.auth.Entidade.enums.contaPrevilegio;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class authService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private utilJwt utilJwt;

    @Autowired
    private cadastroRep usuarioRep;

    @Value("${google.client.id:SEU_CLIENT_ID_AQUI}")
    private String googleClientId;

    public acessDTO loginGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                Optional<cadastroEnt> optionalUser = usuarioRep.findByLogin(email);
                cadastroEnt user;
                if (optionalUser.isEmpty()) {
                    user = new cadastroEnt();
                    user.setEmail(email);
                    user.setLogin(email);
                    user.setNome(name != null ? name : "Google User");
                    user.setSenha(UUID.randomUUID().toString()); // Random password
                    user.setSituacao(estadoCadastro.ACTIVO);
                    user.setContaPrevilegio(contaPrevilegio.GRATIS);
                    // contato is null
                    user = usuarioRep.save(user);
                } else {
                    user = optionalUser.get();
                }

                UsuDetImp userAutenticado = UsuDetImp.build(user);
                if (!userAutenticado.isAccountNonLocked()) {
                    throw new BadCredentialsException("Conta não está ativa");
                }

                String token = utilJwt.gerarToken(userAutenticado);
                return new acessDTO(token);
            } else {
                throw new BadCredentialsException("Token Google inválido");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("Falha na autenticação com o Google: " + e.getMessage());
        }
    }

    public acessDTO login(authDTO authDTO) {
        try {
            UsernamePasswordAuthenticationToken userAut = new UsernamePasswordAuthenticationToken(authDTO.getUsername(),
                    authDTO.getPassword());

            Authentication authentication = authenticationManager.authenticate(userAut);

            UsuDetImp userAutenticado = (UsuDetImp) authentication.getPrincipal();

            if (!userAutenticado.isAccountNonLocked()) {
                throw new BadCredentialsException("Conta não está ativa");
            }

            String token = utilJwt.gerarToken(userAutenticado);

            return new acessDTO(token);

        } catch (BadCredentialsException e) {
            return null;
        }
    }
}

