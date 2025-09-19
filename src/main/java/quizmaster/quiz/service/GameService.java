package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import quizmaster.quiz.dto.AnswerResponse;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;
import quizmaster.quiz.models.RoomPlayer;
import quizmaster.quiz.models.Category;

@Service
@RequiredArgsConstructor
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final GameResultRepository gameResultRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    
    private final GameQuestionRepository gameQuestionRepository; // Added field
    private final GameCategoryQuestionRepository gameCategoryQuestionRepository;
    public Game createGame(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        Game game = new Game();
        game.setRoom(room);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());
        
        return gameRepository.save(game);
    }
    
    public void startGame(Room room) {
        Game game = new Game();
        game.setRoom(room);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());
        
        gameRepository.save(game);
        
        // Inicializar resultados para cada jogador
        room.getPlayers().forEach(roomPlayer -> {
            GameResult result = new GameResult();
            result.setGame(game);
            result.setUser(roomPlayer.getUser());
            result.setTeam(roomPlayer.getTeam());
            gameResultRepository.save(result);
        });

    // Gerar sequência de perguntas global inicial (por enquanto via categorias da sala)
    generateGameQuestions(game, room);
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
            throw new RuntimeException("Jogador não possui categoria atribuída");
        }

        Question question;
        // Se veio questionId, validar que pertence à categoria do jogador
        if (request.getQuestionId() != null) {
            question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));
            if (!assignedCategory.equals(question.getCategory())) {
                throw new RuntimeException("Pergunta não pertence à categoria do jogador");
            }
        } else {
            // Selecionar próxima pergunta não respondida da categoria
            List<Answer> prevAnswers = answerRepository.findByGameAndUser(game, user);
            java.util.Set<Long> answeredIds = prevAnswers.stream().map(a -> a.getQuestion().getId()).collect(java.util.stream.Collectors.toSet());
            List<Question> pool = questionRepository.findRandomByCategory(assignedCategory.getId(), room.getDifficulty().name());
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
        if (!isCorrect) return 0;
        
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
        response.setDifficulty(question.getDifficulty());
        response.setPoints(question.getPoints());
        // Não incluir a resposta correta!
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
        response.setStartTime(game.getStartedAt() != null ? 
            game.getStartedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
        response.setEndTime(game.getEndedAt() != null ? 
            game.getEndedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
        
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

        // Categoria atribuída ao jogador (prioridade) senão preferida, senão todas da sala
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
        throw new RuntimeException("Player has no assigned category");
    }
    // Lista de perguntas já geradas por categoria (se quisermos gerar antecipadamente no futuro)
    // Por ora, seleciona dinamicamente a próxima não respondida dessa categoria
    List<Answer> playerAnswers = answerRepository.findByGameAndUser(game, roomPlayer.getUser());
    Set<Long> answeredIds = playerAnswers.stream().map(a -> a.getQuestion().getId()).collect(Collectors.toSet());
    List<Question> pool = questionRepository.findRandomByCategory(cat.getId(), room.getDifficulty().name());
    Question next = pool.stream().filter(q -> !answeredIds.contains(q.getId())).findFirst()
        .orElseThrow(() -> new RuntimeException("Sem novas perguntas para categoria do jogador"));
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
        
        game.setStatus(GameStatus.FINISHED);
        game.setEndedAt(LocalDateTime.now());
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
            if (fallback.size() > limit) fallback = fallback.subList(0, limit);
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
                if (sequence.size() >= limit) break outer;
                Category cat = rp.getAssignedCategory();
                if (cat == null) continue;
                List<Question> candidates = questionRepository.findRandomByCategory(cat.getId(), room.getDifficulty().name());
                Question chosen = null;
                for (Question q : candidates) {
                    if (!used.contains(q.getId())) { chosen = q; break; }
                }
                if (chosen == null) {
                    for (Question q : fallbackPool) {
                        if (!used.contains(q.getId())) { chosen = q; break; }
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
            if (!added) break; // evita loop infinito
        }
    }
}
