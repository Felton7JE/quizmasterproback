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
import quizmaster.quiz.models.Question;
import quizmaster.quiz.models.Room;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.AnswerRepository;
import quizmaster.quiz.repository.GameRepository;
import quizmaster.quiz.repository.GameResultRepository;
import quizmaster.quiz.repository.QuestionRepository;
import quizmaster.quiz.repository.RoomRepository;
import quizmaster.quiz.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    }
    
    public List<QuestionResponse> getGameQuestions(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));
        
        Room room = game.getRoom();
        
        // Buscar perguntas baseadas nas configurações da sala
        List<String> categoryNames = room.getCategories().stream()
                .map(category -> category.getName())
                .collect(Collectors.toList());
        
        List<Question> questions = questionRepository.findRandomQuestions(
                categoryNames,
                room.getDifficulty().name()
        );
        
        return questions.stream()
                .map(this::convertToQuestionResponse)
                .collect(Collectors.toList());
    }
    
    public AnswerResponse submitAnswer(Long gameId, SubmitAnswerRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));
        
        // Verificar se já respondeu
        if (answerRepository.existsByGameAndUserAndQuestion(game, user, question)) {
            throw new RuntimeException("Pergunta já foi respondida");
        }
        
        boolean isCorrect = question.getCorrectAnswer().equals(request.getSelectedAnswer());
        
        // Calcular pontos baseado no tempo
        Integer points = calculatePoints(question, request.getTimeToAnswer(), isCorrect);
        
        // Salvar resposta
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
        response.setId(game.getId());
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
        
        // Rest of your logic...
        return getGameQuestions(gameId);
    }
    
    public QuestionResponse getCurrentQuestion(Long gameId, Integer questionIndex) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        
        // TODO: Implement proper question retrieval
        List<Question> questions = questionRepository.findAll();
        
        if (questionIndex >= questions.size()) {
            throw new RuntimeException("Question index out of bounds");
        }
        
        return convertToQuestionResponse(questions.get(questionIndex));
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
}
