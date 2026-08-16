package quizmaster.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import quizmaster.quiz.dto.GenerateStudyQuizRequest;
import quizmaster.quiz.dto.StudyQuizResponse;
import quizmaster.quiz.service.StudyService;
import quizmaster.quiz.service.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final quizmaster.quiz.repository.UserRepository userRepository;

    /**
     * Upload de arquivo PDF para extração e geração automática de Quiz com IA
     */
    @PostMapping(value = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPdfAndGenerate(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "difficulty", required = false, defaultValue = "MEDIO") String difficulty,
            @RequestParam(value = "questionCount", required = false, defaultValue = "10") Integer questionCount,
            @RequestParam(value = "userId", required = false) Long userId) {

        try {
            log.info("Recebido upload de PDF: {} ({} bytes) para geração de quiz", 
                    file.getOriginalFilename(), file.getSize());

            // 1. Extrair texto do PDF usando Apache PDFBox
            String extractedText = studyService.extractTextFromPdf(file);

            // 2. Deduzir título ou tópico se não informado
            String finalTitle = (title != null && !title.trim().isEmpty())
                    ? title.trim()
                    : "Quiz: " + (file.getOriginalFilename() != null ? file.getOriginalFilename().replace(".pdf", "") : "Documento PDF");

            String finalTopic = (topic != null && !topic.trim().isEmpty()) ? topic.trim() : "Estudo";

            // 3. Montar requisição de geração
            GenerateStudyQuizRequest request = GenerateStudyQuizRequest.builder()
                    .title(finalTitle)
                    .content(extractedText)
                    .topic(finalTopic)
                    .difficulty(difficulty)
                    .questionCount(questionCount)
                    .userId(userId)
                    .sourceFileName(file.getOriginalFilename())
                    .sourceType("PDF")
                    .build();

            // 4. Gerar quiz com IA / Fallback
            StudyQuizResponse response = studyService.generateQuiz(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Erro de validação no PDF: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro no processamento do PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao processar PDF: " + e.getMessage()));
        }
    }

    /**
     * Geração direta de Quiz por Tema Livre ou Texto colado
     */
    @PostMapping("/generate-quiz")
    public ResponseEntity<?> generateQuiz(@RequestBody GenerateStudyQuizRequest request) {
        try {
            if (request.getSourceType() == null) {
                request.setSourceType("TEXT");
            }
            StudyQuizResponse response = studyService.generateQuiz(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Erro de validação na geração de quiz: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro na geração do quiz: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha ao gerar quiz: " + e.getMessage()));
        }
    }

    /**
     * Consumo de 10 de energia para iniciar uma partida de quiz de estudo
     */
    @PostMapping("/consume-energy")
    public ResponseEntity<?> consumeEnergy(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuário obrigatório."));
            }
            quizmaster.quiz.models.User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuário não encontrado."));
            }
            if (user.getEnergy() == null || user.getEnergy() < 10) {
                return ResponseEntity.badRequest().body(Map.of("error", "Energia insuficiente (mínimo 10 ⚡)."));
            }
            user.setEnergy(user.getEnergy() - 10);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "remainingEnergy", user.getEnergy()));
        } catch (Exception e) {
            log.error("Erro ao debitar energia de estudo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Busca quiz compartilhado pelo código de partilha (ex: STUDY-84920)
     */
    @GetMapping("/shared/{shareCode}")
    public ResponseEntity<?> getSharedQuiz(@PathVariable String shareCode) {
        StudyQuizResponse quiz = studyService.getSharedQuiz(shareCode);
        if (quiz == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Nenhum quiz de estudo encontrado com o código " + shareCode));
        }
        return ResponseEntity.ok(quiz);
    }

    /**
     * Gravação de pontuação e recompensa de estudo para o jogador
     */
    @PostMapping("/record-score")
    public ResponseEntity<?> recordScore(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
            Integer score = payload.get("score") != null ? Integer.valueOf(payload.get("score").toString()) : 0;
            Integer correctCount = payload.get("correctCount") != null ? Integer.valueOf(payload.get("correctCount").toString()) : 0;

            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    int xpGained = Math.max(10, score / 5);
                    int coinsGained = Math.max(2, correctCount);

                    user.setXp((user.getXp() != null ? user.getXp() : 0) + xpGained);
                    user.setCoins((user.getCoins() != null ? user.getCoins() : 0) + coinsGained);
                    user.setTotalPoints((user.getTotalPoints() != null ? user.getTotalPoints() : 0) + score);
                    userRepository.save(user);

                    log.info("Sessão de estudo concluída para o usuário {}: +{} XP, +{} moedas", 
                            user.getUsername(), xpGained, coinsGained);
                });

                int xpGained = Math.max(10, score / 5);
                int coinsGained = Math.max(2, correctCount);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "xpGained", xpGained,
                        "coinsGained", coinsGained
                ));
            }

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Erro ao registrar score de estudo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao registrar pontuação: " + e.getMessage()));
        }
    }
}
