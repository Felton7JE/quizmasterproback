package quizmaster.quiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import quizmaster.quiz.dto.*;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final quizmaster.quiz.repository.UserRepository userRepository;
    private final quizmaster.quiz.repository.UserSeasonProgressRepository userSeasonProgressRepository;

    @Value("${ai.provider:gemini}")
    private String aiProvider;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-flash-latest}")
    private String geminiModel;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openAiModel;

    // Cache em memória para quizzes compartilhados e recentes
    private final Map<String, StudyQuizResponse> quizStorage = new ConcurrentHashMap<>();

    // Cache de tópicos inteligente (48h)
    private final Map<String, CachedQuizEntry> topicQuizCache = new ConcurrentHashMap<>();

    // Rate Limiter Token Bucket para Gemini (Máx. 14 RPM)
    private final java.util.concurrent.atomic.AtomicInteger geminiTokens = new java.util.concurrent.atomic.AtomicInteger(14);
    private volatile long lastGeminiRefillTime = System.currentTimeMillis();

    private static class CachedQuizEntry {
        final StudyQuizResponse response;
        final long timestamp;
        CachedQuizEntry(StudyQuizResponse response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Extrai texto limpo de um arquivo PDF usando Apache PDFBox
     */
    public String extractTextFromPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo PDF enviado está vazio.");
        }

        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {

            if (document.isEncrypted()) {
                throw new IllegalStateException("O arquivo PDF está protegido por senha e não pode ser lido.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);

            if (rawText == null || rawText.trim().isEmpty()) {
                throw new IllegalStateException("Nenhum texto legível foi encontrado no PDF (pode ser uma imagem escaneada).");
            }

            // Limpeza e normalização do texto
            String cleanedText = rawText
                    .replaceAll("[\\r\\n]+", "\n")
                    .replaceAll("[ \\t]+", " ")
                    .trim();

            log.info("PDF processado com sucesso. Páginas: {}, Caracteres extraídos: {}", 
                    document.getNumberOfPages(), cleanedText.length());

            return cleanedText;

        } catch (Exception e) {
            log.error("Erro ao extrair texto do PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao processar arquivo PDF: " + e.getMessage());
        }
    }

    /**
     * Adquire token do bucket para garantir no máximo 14 chamadas por minuto ao Gemini
     */
    private synchronized void acquireGeminiToken() {
        long now = System.currentTimeMillis();
        if (now - lastGeminiRefillTime > 60000) {
            geminiTokens.set(14);
            lastGeminiRefillTime = now;
        }
        while (geminiTokens.get() <= 0) {
            long waitMs = 60000 - (System.currentTimeMillis() - lastGeminiRefillTime);
            if (waitMs > 0) {
                try {
                    log.info("Rate limiter ativo para Gemini. Aguardando {} ms...", Math.min(waitMs + 100, 5000));
                    Thread.sleep(Math.min(waitMs + 100, 5000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (System.currentTimeMillis() - lastGeminiRefillTime > 60000) {
                geminiTokens.set(14);
                lastGeminiRefillTime = System.currentTimeMillis();
            }
        }
        geminiTokens.decrementAndGet();
    }

    /**
     * Gera um Quiz de Estudo completo (via LLM Gemini/OpenAI ou gerador semântico local resiliente)
     */
    public StudyQuizResponse generateQuiz(GenerateStudyQuizRequest request) {
        String content = request.getContent() != null ? request.getContent().trim() : "";
        String topic = request.getTopic() != null && !request.getTopic().trim().isEmpty() ? request.getTopic().trim() : "Geral";
        String title = request.getTitle() != null && !request.getTitle().trim().isEmpty() ? request.getTitle().trim() : "Quiz de Estudo: " + topic;
        int targetQuestions = request.getQuestionCount() != null && request.getQuestionCount() > 0 ? request.getQuestionCount() : 10;
        String difficulty = request.getDifficulty() != null ? request.getDifficulty().toUpperCase() : "MEDIO";

        // ─────────────────────────────────────────────────────────────────────
        // 1. VALIDAÇÃO DE USUÁRIO: LIMITES DIÁRIOS, COOLDOWN E CRISTAIS
        // ─────────────────────────────────────────────────────────────────────
        quizmaster.quiz.models.User user = null;
        boolean isVip = false;

        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
            if (user != null) {
                isVip = userSeasonProgressRepository.existsByUserIdAndSeason_ActiveTrueAndIsPremiumPassTrue(user.getId());

                // Reset de cota diária se for um novo dia
                java.time.LocalDate today = java.time.LocalDate.now();
                if (user.getLastAiQuizDate() == null || !user.getLastAiQuizDate().isEqual(today)) {
                    user.setDailyAiQuizCount(0);
                    user.setLastAiQuizDate(today);
                }

                int currentDailyCount = user.getDailyAiQuizCount() != null ? user.getDailyAiQuizCount() : 0;
                int maxDailyAllowed = isVip ? 10 : 5;

                // Validação de Limite Diário
                if (currentDailyCount >= maxDailyAllowed) {
                    throw new IllegalStateException("Limite diário de " + maxDailyAllowed + " quizzes atingido" 
                            + (isVip ? " (Plano VIP)" : "") + ". Volte amanhã para novos quizzes!");
                }

                // Validação de Cooldown (VIP: 3 min / Free: 6 min)
                if (user.getLastAiQuizTimestamp() != null) {
                    long secondsSinceLast = java.time.temporal.ChronoUnit.SECONDS.between(user.getLastAiQuizTimestamp(), LocalDateTime.now());
                    long requiredCooldownSeconds = isVip ? (3 * 60) : (6 * 60);

                    if (secondsSinceLast < requiredCooldownSeconds) {
                        long remaining = requiredCooldownSeconds - secondsSinceLast;
                        long min = remaining / 60;
                        long sec = remaining % 60;
                        String timeMsg = (min > 0 ? min + " min " : "") + sec + " seg";
                        throw new IllegalStateException("Aguarde " + timeMsg + " para gerar outro quiz (" 
                                + (isVip ? "Cooldown VIP de 3 min" : "Cooldown de 6 min") + ").");
                    }
                }

                // Validação e Dedução de Cristais (apenas para Free)
                if (!isVip) {
                    int currentCrystals = user.getCrystals() != null ? user.getCrystals() : 0;
                    if (currentCrystals < 5) {
                        throw new IllegalStateException("Precisas de pelo menos 5 Cristais 🔮 para gerar este quiz com IA.");
                    }
                    user.setCrystals(currentCrystals - 5);
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // 2. VERIFICAÇÃO DE CACHE DE TÓPICOS (48 HORAS)
        // ─────────────────────────────────────────────────────────────────────
        String cacheKey = (topic + "_" + difficulty + "_" + targetQuestions + "_" + (content.isEmpty() ? "NONE" : String.valueOf(content.hashCode()))).toLowerCase();
        CachedQuizEntry cached = topicQuizCache.get(cacheKey);
        long nowMs = System.currentTimeMillis();

        if (cached != null && (nowMs - cached.timestamp < 48 * 3600 * 1000L)) {
            log.info("Quiz retornado instantaneamente do Cache de Tópicos (0 RPM consumido) para: {}", topic);
            StudyQuizResponse cachedResponse = cached.response;
            String newQuizId = "quiz_" + System.currentTimeMillis() + "_" + (new Random().nextInt(8999) + 1000);
            String newShareCode = "STUDY-" + (new Random().nextInt(89999) + 10000);

            StudyQuizResponse cloned = StudyQuizResponse.builder()
                    .id(newQuizId)
                    .title(title)
                    .description(cachedResponse.getDescription())
                    .sourceType(cachedResponse.getSourceType())
                    .sourceFileName(request.getSourceFileName())
                    .createdAt(LocalDateTime.now())
                    .questionCount(cachedResponse.getQuestionCount())
                    .crystalsCost(isVip ? 0 : 5)
                    .questions(cachedResponse.getQuestions())
                    .flashcards(cachedResponse.getFlashcards())
                    .summaryBullets(cachedResponse.getSummaryBullets())
                    .isShared(true)
                    .shareCode(newShareCode)
                    .build();

            saveQuizToStorage(cloned);
            updateUserAiUsage(user);
            return cloned;
        }

        // ─────────────────────────────────────────────────────────────────────
        // 3. GERAÇÃO COM IA (GEMINI COM TOKEN BUCKET & RETRY) OU FALLBACK LOCAL
        // ─────────────────────────────────────────────────────────────────────
        StudyQuizResponse finalResponse = null;

        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equalsIgnoreCase("SUA_CHAVE_AQUI")) {
            try {
                acquireGeminiToken();
                finalResponse = callGeminiForQuiz(title, topic, content, targetQuestions, difficulty, request.getSourceFileName(), request.getSourceType());
                if (finalResponse != null && finalResponse.getQuestions() != null && !finalResponse.getQuestions().isEmpty()) {
                    topicQuizCache.put(cacheKey, new CachedQuizEntry(finalResponse));
                }
            } catch (Exception e) {
                log.warn("Falha ou indisponibilidade na API Gemini ({}). Ativando Motor Semântico Local...", e.getMessage());
            }
        }

        if (finalResponse == null || finalResponse.getQuestions() == null || finalResponse.getQuestions().isEmpty()) {
            finalResponse = generateLocalSemanticQuiz(title, topic, content, targetQuestions, difficulty, request.getSourceFileName(), request.getSourceType());
        }

        if (isVip) {
            finalResponse.setCrystalsCost(0);
        }

        saveQuizToStorage(finalResponse);
        updateUserAiUsage(user);
        return finalResponse;
    }

    private void updateUserAiUsage(quizmaster.quiz.models.User user) {
        if (user != null) {
            user.setDailyAiQuizCount((user.getDailyAiQuizCount() != null ? user.getDailyAiQuizCount() : 0) + 1);
            user.setLastAiQuizDate(java.time.LocalDate.now());
            user.setLastAiQuizTimestamp(LocalDateTime.now());
            userRepository.save(user);
            log.info("Uso de IA registrado para usuário ID {}. Quizzes hoje: {}", user.getId(), user.getDailyAiQuizCount());
        }
    }

    /**
     * Busca quiz compartilhado por código (ex: STUDY-12345)
     */
    public StudyQuizResponse getSharedQuiz(String shareCode) {
        if (shareCode == null || shareCode.trim().isEmpty()) {
            return null;
        }
        String cleanCode = shareCode.trim().toUpperCase();
        return quizStorage.get(cleanCode);
    }

    /**
     * Salva um quiz no repositório / cache
     */
    public void saveQuizToStorage(StudyQuizResponse quiz) {
        if (quiz == null) return;
        if (quiz.getShareCode() != null) {
            quizStorage.put(quiz.getShareCode().toUpperCase(), quiz);
        }
        if (quiz.getId() != null) {
            quizStorage.put(quiz.getId(), quiz);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTEGRAÇÃO COM GOOGLE GEMINI API
    // ─────────────────────────────────────────────────────────────────────────

    private StudyQuizResponse callGeminiForQuiz(
            String title, String topic, String content, int questionCount, String difficulty, String sourceFileName, String sourceType) {
        
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey.trim();

        String prompt = buildPrompt(title, topic, content, questionCount, difficulty);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "responseMimeType", "application/json"
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseGeminiResponse(response.getBody(), title, topic, sourceFileName, sourceType, questionCount);
        }

        throw new RuntimeException("Resposta inválida do Gemini: " + response.getStatusCode());
    }

    private String buildPrompt(String title, String topic, String content, int questionCount, String difficulty) {
        String baseContext = (content != null && !content.isEmpty())
                ? "DOCUMENTO / TEXTO BASE PARA ESTUDO:\n\"\"\"\n" + (content.length() > 25000 ? content.substring(0, 25000) + "..." : content) + "\n\"\"\""
                : "TÓPICO / MATÉRIA DE ESTUDO: " + topic;

        return "Você é um Professor Universitário Especialista e Elaborador de Provas. "
                + "Crie um exame de estudo rigoroso e didático com exatamente " + questionCount + " perguntas de múltipla escolha no nível " + difficulty + ".\n"
                + baseContext + "\n\n"
                + "REGRAS ESTRITAS DE FORMATAÇÃO JSON (responda APENAS com este JSON válido):\n"
                + "{\n"
                + "  \"title\": \"" + title + "\",\n"
                + "  \"summaryBullets\": [\"ponto chave 1\", \"ponto chave 2\", \"ponto chave 3\", \"ponto chave 4\"],\n"
                + "  \"flashcards\": [\n"
                + "    {\"front\": \"Pergunta ou Conceito fundamental?\", \"back\": \"Resposta detalhada e direta.\"}\n"
                + "  ],\n"
                + "  \"questions\": [\n"
                + "    {\n"
                + "      \"questionText\": \"Enunciado claro e contextualizado da pergunta?\",\n"
                + "      \"options\": [\"Alternativa A\", \"Alternativa B\", \"Alternativa C\", \"Alternativa D\"],\n"
                + "      \"correctAnswer\": 0,\n"
                + "      \"explanation\": \"Explicação pedagógica detalhada do Tutor IA: por que esta é a alternativa correta e o que torna as outras incorretas.\",\n"
                + "      \"hint\": \"Dica mnemônica ou direcionamento de raciocínio.\",\n"
                + "      \"topic\": \"" + topic + "\",\n"
                + "      \"difficulty\": \"" + difficulty + "\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n"
                + "Importante: Gere opções plausíveis e educativas. O campo correctAnswer deve ser um inteiro de 0 a 3 indicando o índice exato no array de options.";
    }

    private StudyQuizResponse parseGeminiResponse(
            String rawJson, String defaultTitle, String topic, String sourceFileName, String sourceType, int targetCount) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode candidates = rootNode.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new RuntimeException("Nenhum candidato retornado pelo Gemini");
            }

            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            String jsonText = textNode.asText();

            // Limpa formatação markdown se houver
            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.substring(3);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }
            jsonText = jsonText.trim();

            JsonNode parsedData = objectMapper.readTree(jsonText);

            String title = parsedData.has("title") ? parsedData.get("title").asText() : defaultTitle;
            List<String> summaryBullets = new ArrayList<>();
            if (parsedData.has("summaryBullets")) {
                parsedData.get("summaryBullets").forEach(b -> summaryBullets.add(b.asText()));
            }

            List<FlashcardDto> flashcards = new ArrayList<>();
            if (parsedData.has("flashcards")) {
                int fId = 1;
                for (JsonNode fNode : parsedData.get("flashcards")) {
                    flashcards.add(FlashcardDto.builder()
                            .id("fc_" + fId++)
                            .front(fNode.path("front").asText())
                            .back(fNode.path("back").asText())
                            .topic(topic)
                            .build());
                }
            }

            List<StudyQuestionDto> questions = new ArrayList<>();
            if (parsedData.has("questions")) {
                int qId = 1;
                for (JsonNode qNode : parsedData.get("questions")) {
                    List<String> options = new ArrayList<>();
                    qNode.path("options").forEach(opt -> options.add(opt.asText()));

                    int correct = qNode.path("correctAnswer").asInt(0);
                    if (correct < 0 || correct >= options.size()) correct = 0;

                    questions.add(StudyQuestionDto.builder()
                            .id("sq_" + qId++)
                            .questionText(qNode.path("questionText").asText())
                            .options(options)
                            .correctAnswer(correct)
                            .explanation(qNode.path("explanation").asText("Resposta baseada no conteúdo estudado."))
                            .hint(qNode.path("hint").asText("Revise os conceitos fundamentais do texto."))
                            .topic(qNode.path("topic").asText(topic))
                            .difficulty(qNode.path("difficulty").asText("MEDIO"))
                            .build());
                }
            }

            String quizId = "quiz_" + System.currentTimeMillis() + "_" + (new Random().nextInt(8999) + 1000);
            String shareCode = "STUDY-" + (new Random().nextInt(89999) + 10000);

            return StudyQuizResponse.builder()
                    .id(quizId)
                    .title(title)
                    .description("Gerado por Inteligência Artificial a partir de " + (sourceFileName != null ? sourceFileName : "material de estudo") + ".")
                    .sourceType(sourceType != null ? sourceType : (sourceFileName != null ? "PDF" : "TEXT"))
                    .sourceFileName(sourceFileName)
                    .createdAt(LocalDateTime.now())
                    .questionCount(questions.size())
                    .crystalsCost(5)
                    .questions(questions)
                    .flashcards(flashcards)
                    .summaryBullets(summaryBullets)
                    .isShared(true)
                    .shareCode(shareCode)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao analisar JSON retornado pelo Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na interpretação da resposta da IA: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GERADOR SEMÂNTICO LOCAL DE FALLBACK (NLP RESILIENTE)
    // ─────────────────────────────────────────────────────────────────────────

    private StudyQuizResponse generateLocalSemanticQuiz(
            String title, String topic, String content, int targetCount, String difficulty, String sourceFileName, String sourceType) {

        List<StudyQuestionDto> questions = new ArrayList<>();
        List<FlashcardDto> flashcards = new ArrayList<>();
        List<String> summaryBullets = new ArrayList<>();

        if (content == null || content.trim().isEmpty()) {
            content = "O estudo estruturado de " + topic + " envolve conceitos fundamentais, teorias clássicas e aplicações práticas. "
                    + "A compreensão dos princípios essenciais permite resolver problemas complexos e fixar o conhecimento para exames.";
        }

        // Separa sentenças estruturadas
        List<String> sentences = extractKeySentences(content);

        // Gera Bullet points de resumo
        for (int i = 0; i < Math.min(4, sentences.size()); i++) {
            summaryBullets.add(sentences.get(i));
        }

        // Gera Flashcards
        for (int i = 0; i < Math.min(6, sentences.size()); i++) {
            String s = sentences.get(i);
            String concept = extractConcept(s, topic);
            flashcards.add(FlashcardDto.builder()
                    .id("fc_local_" + (i + 1))
                    .front("Qual a definição ou papel de: " + concept + "?")
                    .back(s)
                    .topic(topic)
                    .build());
        }

        // Gera Perguntas
        int count = Math.min(targetCount, Math.max(5, sentences.size()));
        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            String primarySentence = sentences.get(i % sentences.size());
            String concept = extractConcept(primarySentence, topic);

            String questionText = "Em relação a " + topic + ", assinale a afirmação correta sobre " + concept + ":";

            List<String> options = new ArrayList<>();
            options.add(primarySentence); // Correta

            // Cria distratores plausíveis baseados em outras sentenças
            options.add("Diz respeito a um processo secundário sem impacto na estrutura de " + topic + ".");
            options.add("Aplica-se unicamente em cenários isolados, contrariando o princípio fundamental da matéria.");
            options.add("Foi substituído em teorias modernas, não possuindo validade prática para " + topic + ".");

            // Embaralha opções
            Collections.shuffle(options, rand);
            int correctIndex = options.indexOf(primarySentence);

            questions.add(StudyQuestionDto.builder()
                    .id("sq_local_" + (i + 1))
                    .questionText(questionText)
                    .options(options)
                    .correctAnswer(correctIndex)
                    .explanation("Correta: " + primarySentence + ". Esta afirmação reflete com precisão os conceitos apresentados no material de estudo.")
                    .hint("Lembre-se da relação direta entre " + concept + " e os fundamentos de " + topic + ".")
                    .topic(topic)
                    .difficulty(difficulty)
                    .build());
        }

        String quizId = "quiz_local_" + System.currentTimeMillis();
        String shareCode = "STUDY-" + (rand.nextInt(89999) + 10000);

        return StudyQuizResponse.builder()
                .id(quizId)
                .title(title)
                .description("Gerado pelo Motor Inteligente a partir de " + (sourceFileName != null ? sourceFileName : "material de estudo") + ".")
                .sourceType(sourceType != null ? sourceType : (sourceFileName != null ? "PDF" : "TEXT"))
                .sourceFileName(sourceFileName)
                .createdAt(LocalDateTime.now())
                .questionCount(questions.size())
                .crystalsCost(5)
                .questions(questions)
                .flashcards(flashcards)
                .summaryBullets(summaryBullets)
                .isShared(true)
                .shareCode(shareCode)
                .build();
    }

    private List<String> extractKeySentences(String text) {
        List<String> result = new ArrayList<>();
        String[] parts = text.split("(?<=[.!?])\\s+");
        for (String p : parts) {
            String trimmed = p.trim().replaceAll("\\s+", " ");
            if (trimmed.length() >= 30 && trimmed.length() <= 250) {
                result.add(trimmed);
            }
        }
        if (result.isEmpty()) {
            result.add(text.length() > 100 ? text.substring(0, 100) : text);
        }
        return result;
    }

    private String extractConcept(String sentence, String fallbackTopic) {
        Pattern pattern = Pattern.compile("(?i)(?:o\\s+|a\\s+|os\\s+|as\\s+|um\\s+|uma\\s+)?([A-ZÀ-Úa-zà-ú0-9\\-\\s]{3,25})(?:\\s+é|\\s+são|\\s+consiste|\\s+ocorre|\\s+representa|\\s+tem|\\s+permite)");
        Matcher matcher = pattern.matcher(sentence);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        String[] words = sentence.split("\\s+");
        if (words.length >= 3) {
            return words[0] + " " + words[1] + " " + words[2];
        }
        return fallbackTopic;
    }
}
