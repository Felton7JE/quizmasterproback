package quizmaster.quiz.auth.Servico;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.auth.DTO.cadastroDTO;
import quizmaster.quiz.auth.Entidade.cadastroEnt;
import quizmaster.quiz.auth.Entidade.cadastroVerEnt;
import quizmaster.quiz.auth.Entidade.enums.contaPrevilegio;
import quizmaster.quiz.auth.Entidade.enums.estadoCadastro;
import quizmaster.quiz.auth.Repositorio.cadastroRep;
import quizmaster.quiz.auth.Repositorio.cadastroVerRep;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class cadastroSer {

    private final PasswordEncoder passwordEncoder;
    private final cadastroVerRep cadastroVerRep;
    private final cadastroRep usuarioRep;
    private final emailSer emailSer;

    public cadastroSer(PasswordEncoder passwordEncoder, cadastroVerRep cadastroVerRep, cadastroRep usuarioRep,
            emailSer emailSer) {
        this.passwordEncoder = passwordEncoder;
        this.cadastroVerRep = cadastroVerRep;
        this.usuarioRep = usuarioRep;
        this.emailSer = emailSer;
    }

    private String gerarCodigoAtivacao() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }

    public List<cadastroDTO> listarUsuarios() {
        List<cadastroEnt> usuarios = usuarioRep.findAll();
        return usuarios.stream().map(cadastroDTO::new).toList();
    }

    public void inserirUsuario(cadastroDTO usuario) {
        cadastroEnt cadastroEnt = new cadastroEnt(usuario);
        cadastroEnt.setSenha(passwordEncoder.encode(cadastroEnt.getSenha()));
        usuarioRep.save(cadastroEnt);
    }

    public cadastroDTO alterarUsuario(cadastroDTO usuario) {
        cadastroEnt cadastroEnt = usuarioRep.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para alteração!"));

        cadastroEnt.setNome(usuario.getNome());
        cadastroEnt.setLogin(usuario.getEmail());
        cadastroEnt.setEmail(usuario.getEmail());
        cadastroEnt.setContato(usuario.getContato());

        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            cadastroEnt.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        return new cadastroDTO(usuarioRep.save(cadastroEnt));
    }

    @Transactional
    public void apagarUsuario(Long id) {
        cadastroEnt cadastroEnt = usuarioRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para exclusão!"));

        usuarioRep.delete(cadastroEnt);
    }

    public enum CadastroResultadoStatus {
        SUCESSO,
        EMAIL_DUPLICADO,
        LOGIN_DUPLICADO,
        CONTATO_DUPLICADO,
        ERRO_INESPERADO
    }

    public static class CadastroResultado {
        private final CadastroResultadoStatus status;
        private final String mensagem;

        public CadastroResultado(CadastroResultadoStatus status, String mensagem) {
            this.status = status;
            this.mensagem = mensagem;
        }

        public CadastroResultadoStatus getStatus() {
            return status;
        }

        public String getMensagem() {
            return mensagem;
        }
    }

    @Transactional
    public CadastroResultado inserirNovoCadastro(cadastroDTO usuario) {
        try {
            if (usuarioRep.existsByEmail(usuario.getEmail())) {
                return new CadastroResultado(CadastroResultadoStatus.EMAIL_DUPLICADO,
                        "O e-mail já está registrado. Por favor, use um e-mail diferente.");
            }

            if (usuarioRep.existsByLogin(usuario.getEmail())) {
                return new CadastroResultado(CadastroResultadoStatus.LOGIN_DUPLICADO,
                        "O nome de usuário já está registrado. Por favor, escolha um nome de usuário diferente.");
            }

            if (usuarioRep.existsByContato(usuario.getContato())) {
                return new CadastroResultado(CadastroResultadoStatus.CONTATO_DUPLICADO,
                        "O contato já está registrado. Por favor, use um contato diferente.");
            }

            cadastroEnt cadastroEnt = new cadastroEnt(usuario);
            cadastroEnt.setSenha(passwordEncoder.encode(cadastroEnt.getSenha()));
            cadastroEnt.setSituacao(estadoCadastro.PENDENTE);
            cadastroEnt.setContaPrevilegio(contaPrevilegio.GRATIS);
            cadastroEnt.setLogin(usuario.getEmail());
            cadastroEnt savedUser = usuarioRep.save(cadastroEnt);

            cadastroVerEnt verificador = new cadastroVerEnt();
            verificador.setCadastroEnt(savedUser);
            verificador.setUuid(UUID.randomUUID());
            verificador.setDataExpericacao(Instant.now().plusMillis(600000));

            String codigoAtivacao = gerarCodigoAtivacao();
            verificador.setCodigoAtivacao(codigoAtivacao);

            cadastroVerRep.save(verificador);

            String linkVerificacao = "http://localhost:8080/auth/verificarCadastro/" + verificador.getUuid().toString();
            String mensagemEmail = String.format(
                    "Olá, %s.\n\n" +
                            "Obrigado por se cadastrar!\n\n" +
                            "Para ativar sua conta, você pode:\n" +
                            "1. Clicar no link abaixo:\n%s\n\n" +
                            "OU\n\n" +
                            "2. Inserir o seguinte código de ativação na página de ativação:\n" +
                            "Código: %s\n\n" +
                            "Estes métodos de verificação expiram em 10 minutos.\n\n" +
                            "Atenciosamente,\nEquipe Eloja",
                    usuario.getNome(),
                    linkVerificacao,
                    codigoAtivacao);

            emailSer.enviarEmail(usuario.getEmail(), "Verificação de Conta - Eloja", mensagemEmail);

            return new CadastroResultado(CadastroResultadoStatus.SUCESSO,
                    "Usuário cadastrado com sucesso. Por favor, verifique seu e-mail para ativar a conta.");

        } catch (Exception e) {
            System.err.println("Erro ao cadastrar usuário: " + e.getMessage());
            e.printStackTrace();
            return new CadastroResultado(CadastroResultadoStatus.ERRO_INESPERADO,
                    "Erro ao cadastrar o usuário. Ocorreu um problema inesperado. Tente novamente mais tarde ou contate o suporte: "
                            + e.getMessage());
        }
    }

   
    @Transactional
    public CadastroResultado ativarContaPorCodigo(String codigo) {
        try {
            Optional<cadastroVerEnt> optVerificador = cadastroVerRep.findByCodigoAtivacao(codigo);

            if (optVerificador.isEmpty()) {
                return new CadastroResultado(CadastroResultadoStatus.ERRO_INESPERADO,
                        "Código de ativação inválido ou já utilizado.");
            }

            cadastroVerEnt verificador = optVerificador.get();

            if (verificador.getDataExpericacao().compareTo(Instant.now()) < 0) {
                cadastroVerRep.delete(verificador);
                return new CadastroResultado(CadastroResultadoStatus.ERRO_INESPERADO,
                        "Código de ativação expirado. Por favor, tente se cadastrar novamente ou solicite um novo código.");
            }

            cadastroEnt usuario = verificador.getCadastroEnt();

            if (usuario.getSituacao() == estadoCadastro.ACTIVO) {
                return new CadastroResultado(CadastroResultadoStatus.ERRO_INESPERADO,
                        "Esta conta já foi ativada.");
            }

            usuario.setSituacao(estadoCadastro.ACTIVO);
            usuarioRep.save(usuario);

            cadastroVerRep.delete(verificador);

            return new CadastroResultado(CadastroResultadoStatus.SUCESSO,
                    "Conta ativada com sucesso utilizando o código!");

        } catch (Exception e) {
            System.err.println("Erro ao ativar conta por código: " + e.getMessage());
            e.printStackTrace();
            return new CadastroResultado(CadastroResultadoStatus.ERRO_INESPERADO,
                    "Erro ao ativar a conta. Ocorreu um problema inesperado. Tente novamente mais tarde ou contate o suporte: "
                            + e.getMessage());
        }
    }
}
