package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import quizmaster.quiz.dto.AnswerResponse;
import quizmaster.quiz.dto.GameEventMessage;
import quizmaster.quiz.dto.GameResponse;
import quizmaster.quiz.dto.GameResultResponse;
import quizmaster.quiz.dto.GameStatsResponse;
import quizmaster.quiz.dto.PlayerAnswerResponse;
import quizmaster.quiz.dto.PlayerResultResponse;
import quizmaster.quiz.dto.QuestionResponse;
import quizmaster.quiz.dto.SubmitAnswerRequest;
import quizmaster.quiz.dto.TeamResultResponse;
import quizmaster.quiz.enums.GameMode;
import quizmaster.quiz.enums.GameStatus;
import quizmaster.quiz.enums.Team;
import quizmaster.quiz.models.Answer;
import quizmaster.quiz.models.Game;
import quizmaster.quiz.models.GameResult;
import quizmaster.quiz.models.GameQuestion;
import quizmaster.quiz.models.Question;
import quizmaster.quiz.models.Room;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.AnswerRepository;
import quizmaster.quiz.repository.GameRepository;
import quizmaster.quiz.repository.GameResultRepository;
import quizmaster.quiz.repository.QuestionRepository;
import quizmaster.quiz.repository.RoomRepository;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.repository.GameQuestionRepository;
import quizmaster.quiz.repository.GameCategoryQuestionRepository;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import quizmaster.quiz.models.RoomPlayer;
import quizmaster.quiz.models.Category;


@Service
public class GameService {

    private final GameRepository gameRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final GameResultRepository gameResultRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final GameCategoryQuestionRepository gameCategoryQuestionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate transactionTemplate;

    // Scheduler for Kahoot mode auto-advance
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> kahootSchedulers = new ConcurrentHashMap<>();

    public GameService(
            GameRepository gameRepository,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository,
            GameResultRepository gameResultRepository,
            UserRepository userRepository,
            RoomRepository roomRepository,
            GameQuestionRepository gameQuestionRepository,
            GameCategoryQuestionRepository gameCategoryQuestionRepository,
            SimpMessagingTemplate messagingTemplate,
            PlatformTransactionManager transactionManager) {
        this.gameRepository = gameRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.gameResultRepository = gameResultRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.gameQuestionRepository = gameQuestionRepository;
        this.gameCategoryQuestionRepository = gameCategoryQuestionRepository;
        this.messagingTemplate = messagingTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }


    public Game createGame(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        Game game = new Game();
        game.setRoom(room);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());

        return gameRepository.save(game);
    }

    @Transactional
    public Game startGame(Room room) {
        Game game = new Game();
        game.setRoom(room);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());
        game.setCurrentQuestionIndex(0);

        final Game savedGame = gameRepository.save(game);

        // Inicializar resultados para cada jogador
        room.getPlayers().forEach(roomPlayer -> {
            GameResult result = new GameResult();
            result.setGame(savedGame);
            result.setUser(roomPlayer.getUser());
            result.setTeam(roomPlayer.getTeam());
            result.setCorrectAnswers(0);
            result.setTotalQuestions(0);
            result.setTotalPoints(0);
            result.setAccuracy(0.0);
            gameResultRepository.save(result);
        });

        // Gerar sequência de perguntas global inicial
        generateGameQuestions(savedGame, room);

        // Se for modo KAHOOT, iniciar o scheduler automático
        if (room.getGameMode() == GameMode.KAHOOT) {
            scheduleKahootGame(savedGame, room);
        }

        return savedGame;
    }

    /**
     * Agenda o avanço automático de perguntas para o modo Kahoot.
     * A cada (questionTime + 3) segundos, avança para a próxima pergunta
     * e emite um evento NEXT_QUESTION via WebSocket para todos os clientes.
     * Os 3 segundos extras são para o jogador ver o resultado antes de avançar.
     */
    private void scheduleKahootGame(Game game, Room room) {
        final Long gameId = game.getId();
        final String roomCode = room.getRoomCode();
        final int questionTimeSec = room.getQuestionTime() != null ? room.getQuestionTime() : 15;
        // intervalBetweenQuestions = questionTime + 4s (2s para ver resultado + 2s de ranking parcial)
        final long intervalSec = questionTimeSec + 4L;

        // Buscar a sequência de perguntas gerada
        List<GameQuestion> seq = gameQuestionRepository.findByGameOrdered(gameId);
        final int totalQuestions = seq.size();
        if (totalQuestions == 0) return;

        // Contador de avanço (começa em 1 pois a pergunta 0 já foi enviada no GAME_STARTED)
        final int[] questionIndex = {0};

        // Emite imediatamente a primeira pergunta (index 0)
        emitNextQuestion(roomCode, gameId, seq.get(0), 0, totalQuestions, false);

        // Agenda as próximas perguntas
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            transactionTemplate.execute(status -> {
                questionIndex[0]++;
                try {
                    // Recarregar game do banco para evitar dados stale
                    Game freshGame = gameRepository.findById(gameId).orElse(null);
                    if (freshGame == null || freshGame.getStatus() == GameStatus.FINISHED) {
                        cancelKahootScheduler(gameId);
                        return null;
                    }

                    if (questionIndex[0] >= totalQuestions) {
                        // Fim do jogo
                        freshGame.setStatus(GameStatus.FINISHED);
                        freshGame.setEndedAt(LocalDateTime.now());
                        gameRepository.save(freshGame);

                        GameEventMessage endEvent = new GameEventMessage();
                        endEvent.setType("GAME_ENDED");
                        endEvent.setRoomCode(roomCode);
                        endEvent.setGameId(gameId);
                        messagingTemplate.convertAndSend("/topic/room/" + roomCode, endEvent);
                        cancelKahootScheduler(gameId);
                        return null;
                    }

                    freshGame.setCurrentQuestionIndex(questionIndex[0]);
                    gameRepository.save(freshGame);

                    List<GameQuestion> freshSeq = gameQuestionRepository.findByGameOrdered(gameId);
                    if (questionIndex[0] < freshSeq.size()) {
                        boolean isLast = (questionIndex[0] == totalQuestions - 1);
                        emitNextQuestion(roomCode, gameId, freshSeq.get(questionIndex[0]), questionIndex[0], totalQuestions, isLast);
                    }
                } catch (Exception e) {
                    System.err.println("Kahoot scheduler error for game " + gameId + ": " + e.getMessage());
                }
                return null;
            });
        }, intervalSec, intervalSec, TimeUnit.SECONDS);

        kahootSchedulers.put(gameId, future);
    }

    private void emitNextQuestion(String roomCode, Long gameId, GameQuestion gq, int index, int total, boolean isLast) {
        Question q = gq.getQuestion();
        GameEventMessage event = new GameEventMessage();
        event.setType("NEXT_QUESTION");
        event.setRoomCode(roomCode);
        event.setGameId(gameId);
        event.setQuestionIndex(index);
        event.setTotalQuestions(total);
        event.setIsLastQuestion(isLast);

        // Dados da pergunta (sem revelar a resposta correta)
        java.util.Map<String, Object> questionData = new java.util.HashMap<>();
        questionData.put("id", q.getId());
        questionData.put("questionText", q.getQuestionText());
        questionData.put("options", q.getOptions());
        questionData.put("category", q.getCategory() != null ? q.getCategory().getName() : null);
        questionData.put("difficulty", q.getDifficulty() != null ? q.getDifficulty().name() : null);
        questionData.put("points", q.getPoints());
        // Revela a resposta correta APENAS para o frontend mostrar depois do timer
        questionData.put("correctAnswer", q.getCorrectAnswer());
        event.setQuestionData(questionData);

        messagingTemplate.convertAndSend("/topic/room/" + roomCode, event);
    }

    private void cancelKahootScheduler(Long gameId) {
        ScheduledFuture<?> f = kahootSchedulers.remove(gameId);
        if (f != null) f.cancel(false);
    }


    public AnswerResponse submitAnswer(Long gameId, SubmitAnswerRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        // Obter jogador na sala e categoria atribuída
        Room room = game.getRoom();
        var roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Jogador não pertence à sala"));
        var assignedCategory = roomPlayer.getAssignedCategory();
        if (assignedCategory == null) {
            assignedCategory = roomPlayer.getPreferredCategory();
        }

        Question question;
        // Se veio questionId, validar que pertence à categoria do jogador
        if (request.getQuestionId() != null) {
            question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));
            if (assignedCategory != null && !assignedCategory.equals(question.getCategory())) {
                throw new RuntimeException("Pergunta não pertence à categoria do jogador");
            }
        } else {
            // Selecionar próxima pergunta não respondida da categoria
            List<Answer> prevAnswers = answerRepository.findByGameAndUser(game, user);
            java.util.Set<Long> answeredIds = prevAnswers.stream().map(a -> a.getQuestion().getId())
                    .collect(java.util.stream.Collectors.toSet());
            
            List<Question> pool;
            if (assignedCategory != null) {
                pool = questionRepository.findRandomByCategory(assignedCategory.getId(), room.getDifficulty().name());
            } else {
                List<String> allCatNames = room.getCategories().stream().map(quizmaster.quiz.models.Category::getName).collect(java.util.stream.Collectors.toList());
                pool = questionRepository.findRandomQuestions(allCatNames, room.getDifficulty().name());
            }
            
            question = pool.stream().filter(q -> !answeredIds.contains(q.getId())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Sem novas perguntas para esta categoria"));
        }

        // Verificar duplicidade de resposta
        if (answerRepository.existsByGameAndUserAndQuestion(game, user, question)) {
            throw new RuntimeException("Pergunta já foi respondida");
        }

        boolean isCorrect = question.getCorrectAnswer().equals(request.getSelectedAnswer());
        Integer points = calculatePoints(question, request.getTimeToAnswer(), isCorrect);

        Answer answer = new Answer();
        answer.setGame(game);
        answer.setUser(user);
        answer.setQuestion(question);
        answer.setSelectedAnswer(request.getSelectedAnswer());
        answer.setIsCorrect(isCorrect);
        answer.setPoints(points);
        answer.setTimeToAnswer(request.getTimeToAnswer());
        answer.setAnsweredAt(LocalDateTime.now());

        answerRepository.save(answer);

        // Atualizar resultado do jogo
        GameResult gameResult = gameResultRepository.findByGameAndUser(game, user)
                .orElseThrow(() -> new RuntimeException("Resultado do jogo não encontrado"));

        gameResult.setTotalQuestions(gameResult.getTotalQuestions() + 1);
        if (isCorrect) {
            gameResult.setCorrectAnswers(gameResult.getCorrectAnswers() + 1);
        }
        gameResult.setTotalPoints(gameResult.getTotalPoints() + points);
        gameResult.setAccuracy((double) gameResult.getCorrectAnswers() / gameResult.getTotalQuestions() * 100);

        gameResultRepository.save(gameResult);

        // Preparar resposta
        AnswerResponse response = new AnswerResponse();
        response.setIsCorrect(isCorrect);
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setExplanation(question.getExplanation());
        response.setPoints(points);
        response.setTotalPoints(gameResult.getTotalPoints());

        // Broadcast leaderboard update via WebSocket
        try {
            List<PlayerResultResponse> updatedLeaderboard = getLeaderboard(gameId);
            GameEventMessage lbEvent = new GameEventMessage();
            lbEvent.setType("LEADERBOARD_UPDATE");
            lbEvent.setRoomCode(room.getRoomCode());
            lbEvent.setPayload(updatedLeaderboard);
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomCode(), lbEvent);
        } catch (Exception e) {
            System.err.println("Failed to broadcast leaderboard update: " + e.getMessage());
        }

        return response;
    }

    public GameResultResponse getGameResults(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        List<GameResult> results = gameResultRepository.findByGameOrderByTotalPointsDesc(game);

        GameResultResponse response = new GameResultResponse();
        response.setGameId(gameId);
        response.setGameMode(game.getRoom().getGameMode().name());
        response.setStartedAt(game.getStartedAt());
        response.setEndedAt(game.getEndedAt());

        List<PlayerResultResponse> playerResults = results.stream()
                .map(this::convertToPlayerResultResponse)
                .collect(Collectors.toList());

        response.setResults(playerResults);

        if (!playerResults.isEmpty()) {
            response.setWinner(playerResults.get(0));
        }

        // Calcular resultado das equipes se for modo team
        if (game.getRoom().getGameMode() == GameMode.TEAM) {
            TeamResultResponse teamResults = calculateTeamResults(results);
            response.setTeamResults(teamResults);
        }

        return response;
    }

    public List<PlayerResultResponse> getLeaderboard(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        List<GameResult> results = gameResultRepository.findByGameOrderByTotalPointsDesc(game);

        return results.stream()
                .map(this::convertToPlayerResultResponse)
                .collect(Collectors.toList());
    }

    private Integer calculatePoints(Question question, Long timeToAnswer, boolean isCorrect) {
        if (!isCorrect)
            return 0;

        // Pontuação base da pergunta
        Integer basePoints = question.getPoints();

        // Bonus por velocidade (máximo 50% extra)
        double timeBonus = Math.max(0, 1 - (timeToAnswer / 30000.0)) * 0.5;

        return (int) (basePoints * (1 + timeBonus));
    }

    private QuestionResponse convertToQuestionResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setQuestionText(question.getQuestionText());
        response.setOptions(question.getOptions());
        response.setCorrectAnswer(question.getCorrectAnswer());
        
        if (question.getCategory() != null) {
            Category cat = new Category();
            cat.setId(question.getCategory().getId());
            cat.setName(question.getCategory().getName());
            cat.setDisplayName(question.getCategory().getDisplayName());
            response.setCategory(cat);
        } else {
            response.setCategory(null);
        }
        
        response.setDifficulty(question.getDifficulty());
        response.setPoints(question.getPoints());
        return response;
    }

    private PlayerResultResponse convertToPlayerResultResponse(GameResult result) {
        PlayerResultResponse response = new PlayerResultResponse();
        response.setUserId(result.getUser().getId());
        response.setUsername(result.getUser().getUsername());
        response.setAvatar(result.getUser().getAvatar());
        response.setTeam(result.getTeam() != null ? result.getTeam().name() : null);
        response.setCorrectAnswers(result.getCorrectAnswers());
        response.setTotalQuestions(result.getTotalQuestions());
        response.setTotalPoints(result.getTotalPoints());
        response.setAccuracy(result.getAccuracy());
        response.setBestStreak(result.getBestStreak());
        response.setTotalTime(result.getTotalTime());
        response.setPosition(result.getPosition());
        return response;
    }

    private TeamResultResponse calculateTeamResults(List<GameResult> results) {
        TeamResultResponse teamResults = new TeamResultResponse();

        int teamAPoints = results.stream()
                .filter(r -> r.getTeam() == Team.A)
                .mapToInt(GameResult::getTotalPoints)
                .sum();

        int teamBPoints = results.stream()
                .filter(r -> r.getTeam() == Team.B)
                .mapToInt(GameResult::getTotalPoints)
                .sum();

        int teamACorrect = results.stream()
                .filter(r -> r.getTeam() == Team.A)
                .mapToInt(GameResult::getCorrectAnswers)
                .sum();

        int teamBCorrect = results.stream()
                .filter(r -> r.getTeam() == Team.B)
                .mapToInt(GameResult::getCorrectAnswers)
                .sum();

        teamResults.setTeamAPoints(teamAPoints);
        teamResults.setTeamBPoints(teamBPoints);
        teamResults.setTeamACorrectAnswers(teamACorrect);
        teamResults.setTeamBCorrectAnswers(teamBCorrect);
        teamResults.setWinnerTeam(teamAPoints > teamBPoints ? "A" : "B");

        return teamResults;
    }

    public GameResponse getGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        GameResponse response = new GameResponse();
        response.setGameId(game.getId());
        response.setRoomCode(game.getRoom().getRoomCode());
        response.setStatus(game.getStatus().toString());
        response.setCurrentQuestionIndex(0); // TODO: Implement current question tracking
        response.setTotalQuestions(game.getRoom().getQuestionCount());
        response.setGameMode(game.getRoom().getGameMode().toString());
        response.setDifficulty(game.getRoom().getDifficulty().toString());
        response.setStartTime(game.getStartedAt() != null
                ? game.getStartedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null);
        response.setEndTime(game.getEndedAt() != null
                ? game.getEndedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null);

        return response;
    }

    public List<QuestionResponse> getGameQuestions(Long gameId, Long userId) {
        if (gameId == null) {
            throw new IllegalArgumentException("Game ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado com ID: " + gameId));
        Room room = game.getRoom();

        // Encontrar o jogador dentro da sala
        var roomPlayerOpt = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst();

        if (roomPlayerOpt.isEmpty()) {
            throw new RuntimeException("Jogador não pertence a esta sala");
        }

        var roomPlayer = roomPlayerOpt.get();

        // Categoria atribuída ao jogador (prioridade) senão preferida, senão todas da
        // sala
        quizmaster.quiz.models.Category playerCategory = roomPlayer.getAssignedCategory();
        if (playerCategory == null) {
            playerCategory = roomPlayer.getPreferredCategory();
        }

        List<Question> questions;
        if (playerCategory != null) {
            // Buscar perguntas aleatórias apenas da categoria do jogador
            questions = questionRepository.findRandomByCategory(playerCategory.getId(), room.getDifficulty().name());
        } else {
            // Fallback: usa categorias gerais da sala
            List<String> categoryNames = room.getCategories().stream()
                    .map(c -> c.getName())
                    .collect(java.util.stream.Collectors.toList());
            questions = questionRepository.findRandomQuestions(categoryNames, room.getDifficulty().name());
        }

        // Limitar ao questionCount definido na sala
        int limit = room.getQuestionCount() != null ? room.getQuestionCount() : questions.size();
        if (questions.size() > limit) {
            questions = questions.subList(0, limit);
        }

        return questions.stream().map(this::convertToQuestionResponse).collect(java.util.stream.Collectors.toList());
    }

    public QuestionResponse getCurrentQuestion(Long gameId, Integer questionIndex) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        List<GameQuestion> seq = gameQuestionRepository.findByGameOrdered(gameId);
        if (questionIndex < 0 || questionIndex >= seq.size()) {
            throw new RuntimeException("Invalid question index");
        }
        return convertToQuestionResponse(seq.get(questionIndex).getQuestion());
    }

    public QuestionResponse getCurrentQuestion(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        List<GameQuestion> seq = gameQuestionRepository.findByGameOrdered(gameId);
        int idx = game.getCurrentQuestionIndex() != null ? game.getCurrentQuestionIndex() : 0;
        if (seq.isEmpty()) {
            // Não gerar erro aqui para não quebrar fluxo quando usando modo por categoria
            return null;
        }
        if (idx < 0 || idx >= seq.size()) {
            idx = 0; // fallback
        }
        return convertToQuestionResponse(seq.get(idx).getQuestion());
    }

    public QuestionResponse getCurrentQuestionForPlayer(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        Room room = game.getRoom();
        var roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not in room"));
        Category cat = roomPlayer.getAssignedCategory();
        if (cat == null) {
            cat = roomPlayer.getPreferredCategory();
        }
        
        List<Question> pool;
        if (cat != null) {
            pool = questionRepository.findRandomByCategory(cat.getId(), room.getDifficulty().name());
        } else {
            List<String> allCatNames = room.getCategories().stream().map(Category::getName).collect(Collectors.toList());
            pool = questionRepository.findRandomQuestions(allCatNames, room.getDifficulty().name());
        }

        // Por ora, seleciona dinamicamente a próxima não respondida dessa categoria
        List<Answer> playerAnswers = answerRepository.findByGameAndUser(game, roomPlayer.getUser());
        Set<Long> answeredIds = playerAnswers.stream().map(a -> a.getQuestion().getId()).collect(Collectors.toSet());
        Question next = pool.stream().filter(q -> !answeredIds.contains(q.getId())).findFirst()
                .orElseThrow(() -> new RuntimeException("Sem novas perguntas para o jogador"));
        return convertToQuestionResponse(next);
    }

    public List<PlayerResultResponse> getLiveLeaderboard(Long gameId) {
        return getLeaderboard(gameId);
    }

    public void pauseGame(Long gameId, Long hostId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!game.getRoom().getHost().getId().equals(hostId)) {
            throw new RuntimeException("Only host can pause the game");
        }

        game.setStatus(GameStatus.PAUSED);
        gameRepository.save(game);
    }

    public void resumeGame(Long gameId, Long hostId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!game.getRoom().getHost().getId().equals(hostId)) {
            throw new RuntimeException("Only host can resume the game");
        }

        game.setStatus(GameStatus.IN_PROGRESS);
        gameRepository.save(game);
    }

    public GameResultResponse finishGame(Long gameId, Long hostId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!game.getRoom().getHost().getId().equals(hostId)) {
            throw new RuntimeException("Only host can finish the game");
        }

        // Simulate bot points
        int totalQs = game.getRoom().getQuestionCount() != null ? game.getRoom().getQuestionCount() : 5;
        for (quizmaster.quiz.models.GameResult res : gameResultRepository.findByGameOrderByTotalPointsDesc(game)) {
            if (res.getUser().getUsername().startsWith("Bot_")) {
                int simulatedCorrect = (int) (Math.random() * (totalQs + 1));
                res.setCorrectAnswers(simulatedCorrect);
                res.setTotalQuestions(totalQs);
                res.setTotalPoints(simulatedCorrect * (100 + (int)(Math.random() * 50)));
                res.setAccuracy((double) simulatedCorrect / totalQs * 100);
                res.setTotalTime((long) (Math.random() * 50000));
                gameResultRepository.save(res);
            }
        }

        game.setStatus(GameStatus.FINISHED);
        game.setEndedAt(java.time.LocalDateTime.now());
        gameRepository.save(game);

        return getGameResults(gameId);
    }

    public GameStatsResponse getGameStats(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        List<GameResult> results = game.getResults();

        GameStatsResponse stats = new GameStatsResponse();
        stats.setGameId(gameId);
        stats.setTotalPlayers(results.size());
        stats.setTotalQuestions(game.getRoom().getQuestionCount());
        stats.setQuestionsAnswered(results.stream()
                .mapToInt(GameResult::getTotalQuestions)
                .max().orElse(0));
        stats.setAverageScore(results.stream()
                .mapToDouble(GameResult::getTotalPoints)
                .average().orElse(0.0));

        if (game.getStartedAt() != null && game.getEndedAt() != null) {
            stats.setDuration(java.time.Duration.between(game.getStartedAt(), game.getEndedAt()).toMinutes());
        }

        return stats;
    }

    public List<PlayerAnswerResponse> getPlayerAnswers(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Answer> answers = answerRepository.findByGameAndUser(game, user);

        return answers.stream()
                .map(answer -> {
                    PlayerAnswerResponse response = new PlayerAnswerResponse();
                    response.setQuestionId(answer.getQuestion().getId());
                    response.setQuestion(answer.getQuestion().getQuestionText());
                    response.setPlayerAnswer(String.valueOf(answer.getSelectedAnswer()));
                    response.setCorrectAnswer(String.valueOf(answer.getQuestion().getCorrectAnswer()));
                    response.setIsCorrect(answer.getIsCorrect());
                    response.setTimeToAnswer(answer.getTimeToAnswer().intValue());
                    response.setPointsEarned(answer.getPoints());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public QuestionResponse advanceToNextQuestion(Long gameId, Long hostId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (!game.getRoom().getHost().getId().equals(hostId)) {
            throw new RuntimeException("Only host can advance question");
        }
        List<GameQuestion> seq = gameQuestionRepository.findByGameOrdered(gameId);
        int current = game.getCurrentQuestionIndex() != null ? game.getCurrentQuestionIndex() : 0;
        if (current + 1 >= seq.size()) {
            game.setStatus(GameStatus.FINISHED);
            game.setEndedAt(LocalDateTime.now());
            gameRepository.save(game);
            return null;
        }
        game.setCurrentQuestionIndex(current + 1);
        gameRepository.save(game);
        return convertToQuestionResponse(seq.get(current + 1).getQuestion());
    }

    private void generateGameQuestions(Game game, Room room) {
        int limit = room.getQuestionCount() != null ? room.getQuestionCount() : 10;

        List<RoomPlayer> playersWithCategory = room.getPlayers().stream()
                .filter(p -> p.getAssignedCategory() != null)
                .collect(Collectors.toList());

        if (playersWithCategory.isEmpty()) {
            List<String> catNames = room.getCategories().stream().map(Category::getName).collect(Collectors.toList());
            List<Question> fallback = questionRepository.findRandomQuestions(catNames, room.getDifficulty().name());
            if (fallback.size() > limit)
                fallback = fallback.subList(0, limit);
            int idx = 0;
            for (Question q : fallback) {
                GameQuestion gq = new GameQuestion();
                gq.setGame(game);
                gq.setQuestion(q);
                gq.setOrderIndex(idx++);
                gameQuestionRepository.save(gq);
            }
            return;
        }

        Set<Long> used = new HashSet<>();
        List<GameQuestion> sequence = new ArrayList<>();
        int order = 0;

        List<String> allCatNames = room.getCategories().stream().map(Category::getName).collect(Collectors.toList());
        List<Question> fallbackPool = questionRepository.findRandomQuestions(allCatNames, room.getDifficulty().name());

        outer: while (sequence.size() < limit) {
            boolean added = false;
            for (RoomPlayer rp : playersWithCategory) {
                if (sequence.size() >= limit)
                    break outer;
                Category cat = rp.getAssignedCategory();
                if (cat == null)
                    continue;
                List<Question> candidates = questionRepository.findRandomByCategory(cat.getId(),
                        room.getDifficulty().name());
                Question chosen = null;
                for (Question q : candidates) {
                    if (!used.contains(q.getId())) {
                        chosen = q;
                        break;
                    }
                }
                if (chosen == null) {
                    for (Question q : fallbackPool) {
                        if (!used.contains(q.getId())) {
                            chosen = q;
                            break;
                        }
                    }
                }
                if (chosen != null) {
                    used.add(chosen.getId());
                    GameQuestion gq = new GameQuestion();
                    gq.setGame(game);
                    gq.setQuestion(chosen);
                    gq.setOrderIndex(order++);
                    gameQuestionRepository.save(gq);
                    sequence.add(gq);
                    added = true;
                }
            }
            if (!added)
                break; // evita loop infinito
        }
    }
}
