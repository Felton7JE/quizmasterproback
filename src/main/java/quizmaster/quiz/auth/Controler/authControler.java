package quizmaster.quiz.auth.Controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.auth.DTO.authDTO;
import quizmaster.quiz.auth.DTO.cadastroDTO;
import quizmaster.quiz.auth.DTO.GoogleAuthDTO;
import quizmaster.quiz.auth.Servico.cadastroSer;
import quizmaster.quiz.auth.Servico.cadastroSer.CadastroResultado;
import quizmaster.quiz.auth.Servico.authService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class authControler {

    private final authService authService;
    private final cadastroSer cadastroSer;

    public authControler(authService authService, cadastroSer cadastroSer) {
        this.authService = authService;
        this.cadastroSer = cadastroSer;
    }

    @PostMapping(value = "/novoCadastro")
    public ResponseEntity<Map<String, String>> inserirNovoCadastro(@RequestBody cadastroDTO cadastroDTO) {
        CadastroResultado resultado = cadastroSer.inserirNovoCadastro(cadastroDTO);

        switch (resultado.getStatus()) {
            case SUCESSO:
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensagem", resultado.getMensagem()));
            case EMAIL_DUPLICADO:
            case LOGIN_DUPLICADO:
            case CONTATO_DUPLICADO:
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", resultado.getMensagem()));
            case ERRO_INESPERADO:
                System.err.println("Erro ao cadastrar usuário: " + resultado.getMensagem());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("mensagem", resultado.getMensagem()));
            default:
                System.err.println("Erro inesperado no cadastro: " + resultado.getMensagem());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("mensagem", "Ocorreu um erro inesperado durante o cadastro."));
        }
    }

    
    @PostMapping(value = "/ativarPorCodigo")
    public ResponseEntity<Map<String, String>> ativarPorCodigo(@RequestParam("codigo") String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Código de ativação não fornecido."));
        }

        CadastroResultado resultado = cadastroSer.ativarContaPorCodigo(codigo.trim());

        switch (resultado.getStatus()) {
            case SUCESSO:
                return ResponseEntity.ok(Map.of("mensagem", resultado.getMensagem()));
            case ERRO_INESPERADO:
                System.err.println("Erro ao ativar conta por código: " + resultado.getMensagem());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("mensagem", resultado.getMensagem()));
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", resultado.getMensagem()));
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody authDTO authDTO) {
        try {
            Object loginResult = authService.login(authDTO);
            return ResponseEntity.ok(loginResult);
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login ou senha inválidos.");
        }
    }

    @PostMapping(value = "/google")
    public ResponseEntity<?> loginGoogle(@RequestBody GoogleAuthDTO googleAuthDTO) {
        try {
            Object loginResult = authService.loginGoogle(googleAuthDTO.getIdToken());
            return ResponseEntity.ok(loginResult);
        } catch (Exception e) {
            System.err.println("Google Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falha ao autenticar com o Google.");
        }
    }

}
