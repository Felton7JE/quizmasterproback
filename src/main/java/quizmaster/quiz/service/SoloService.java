package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.dto.*;
import quizmaster.quiz.enums.Difficulty;
import quizmaster.quiz.models.*;
import quizmaster.quiz.repository.*;
import org.springframework.data.domain.PageRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoloService {

    private final UserRepository userRepository;
    private final CategoryEntityRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final UserQuestionHistoryRepository userQuestionHistoryRepository;
    private final SoloLevelProgressRepository soloLevelProgressRepository;
    private final SoloBossProgressRepository soloBossProgressRepository;
    private final SeasonService seasonService;
    private final FreeModeScoreRepository freeModeScoreRepository;

    private static final int TOTAL_MAP_LEVELS = 100;

    private void calculateAndRefreshEnergy(User user) {
        int maxEnergy = 5;
        int rechargeMinutes = 30;

        if (user.getEnergy() == null) user.setEnergy(5);
        if (user.getEnergy() >= maxEnergy) {
            user.setLastEnergyUpdate(LocalDateTime.now());
            return;
        }

        if (user.getLastEnergyUpdate() == null) {
            user.setLastEnergyUpdate(LocalDateTime.now());
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(user.getLastEnergyUpdate(), now);
        long minutesPassed = duration.toMinutes();

        if (minutesPassed >= rechargeMinutes) {
            int energyToAdd = (int) (minutesPassed / rechargeMinutes);
            int newEnergy = Math.min(maxEnergy, user.getEnergy() + energyToAdd);
            user.setEnergy(newEnergy);
            
            user.setLastEnergyUpdate(user.getLastEnergyUpdate().plusMinutes((long) energyToAdd * rechargeMinutes));
        }
    }

    @Transactional
    public SoloMapResponse getMapProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado: " + userId));

        // Inicializar níveis se não existirem
        List<SoloLevelProgress> progressList = soloLevelProgressRepository.findByUserIdOrderByLevelNumberAsc(userId);
        if (progressList.isEmpty()) {
            progressList = initializeMapForUser(user);
        }

        calculateAndRefreshEnergy(user);
        userRepository.save(user);

        int totalStars = progressList.stream().mapToInt(SoloLevelProgress::getStarsCount).sum();
        int maxUnlocked = progressList.stream()
                .filter(SoloLevelProgress::getUnlocked)
                .mapToInt(SoloLevelProgress::getLevelNumber)
                .max().orElse(1);

        List<SoloLevelDto> levelDtos = new ArrayList<>();
        for (SoloLevelProgress p : progressList) {
            SoloLevelDto dto = new SoloLevelDto();
            dto.setLevelNumber(p.getLevelNumber());

            // Atribuir categoria por mundo (5 níveis por mundo)
            Category cat = getCategoryForLevel(p.getLevelNumber());
            dto.setCategoryName(cat != null ? cat.getName() : "Geral");
            dto.setCategoryDisplayName(cat != null ? cat.getDisplayName() : "Conhecimento Geral");

            dto.setDifficulty(getDifficultyForLevel(p.getLevelNumber()).name());
            dto.setUnlocked(p.getUnlocked());
            dto.setCompleted(p.getCompleted());
            dto.setStarsCount(p.getStarsCount());
            dto.setHighScore(p.getHighScore());
            dto.setIsBossLevel(p.getIsBossLevel());

            int requiredStars = 0;
            if (p.getLevelNumber() > 1 && (p.getLevelNumber() - 1) % 5 == 0) {
                requiredStars = (p.getLevelNumber() - 1) * 2;
            }
            dto.setRequiredStarsToUnlock(requiredStars);
            
            if (requiredStars > 0 && totalStars < requiredStars) {
                dto.setUnlocked(false);
            }

            if (p.getIsBossLevel()) {
                SoloBossProgress bossProg = soloBossProgressRepository
                        .findByUserIdAndBossLevelNumber(userId, p.getLevelNumber())
                        .orElseGet(
                                () -> soloBossProgressRepository.save(new SoloBossProgress(user, p.getLevelNumber())));

                dto.setBossName(getBossName(p.getLevelNumber()));
                dto.setBossAvatar(getBossAvatar(p.getLevelNumber()));
                dto.setBossLivesRemaining(bossProg.getLivesRemaining());
            } else {
                dto.setBossName(getBotName(p.getLevelNumber()));
                dto.setBossAvatar("bot_avatar_" + (p.getLevelNumber() % 5 + 1));
                dto.setBossLivesRemaining(3);
            }

            levelDtos.add(dto);
        }

        long secondsUntilNextEnergy = 0;
        if (user.getEnergy() < 5 && user.getLastEnergyUpdate() != null) {
            Duration duration = Duration.between(user.getLastEnergyUpdate(), LocalDateTime.now());
            long secondsPassed = duration.getSeconds();
            secondsUntilNextEnergy = Math.max(0, (30 * 60) - secondsPassed);
        }

        return new SoloMapResponse(userId, maxUnlocked, totalStars, user.getEnergy(), secondsUntilNextEnergy, levelDtos);
    }

    @Transactional
    public SoloStartLevelResponse startLevel(Long userId, Integer levelNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        calculateAndRefreshEnergy(user);
        if (user.getEnergy() < 1) {
            throw new RuntimeException("Energia insuficiente!");
        }

        user.setEnergy(user.getEnergy() - 1);
        if (user.getEnergy() == 4) {
            user.setLastEnergyUpdate(LocalDateTime.now());
        }
        userRepository.save(user);

        SoloLevelProgress progress = soloLevelProgressRepository
                .findByUserIdAndLevelNumber(userId, levelNumber)
                .orElseThrow(() -> new RuntimeException("Nível não encontrado: " + levelNumber));

        if (!progress.getUnlocked()) {
            throw new RuntimeException("Este nível ainda está bloqueado!");
        }

        Category category = getCategoryForLevel(levelNumber);
        Difficulty difficulty = getDifficultyForLevel(levelNumber);
        Long catId = category != null ? category.getId() : null;

        // 1. Buscar 5 perguntas inéditas (filtro anti-repetição)
        List<Question> questions = new ArrayList<>();
        if (catId != null) {
            questions = userQuestionHistoryRepository.findUnseenByCategoryAndDifficulty(userId, catId,
                    difficulty.name(), PageRequest.of(0, 5));
            if (questions.size() < 5) {
                // Fallback para perguntas inéditas apenas da categoria (qualquer dificuldade)
                List<Question> extra = userQuestionHistoryRepository.findUnseenByCategory(userId, catId,
                        PageRequest.of(0, 5 - questions.size()));
                questions.addAll(extra);
            }
        }

        if (questions.size() < 5) {
            // Se ainda assim faltarem perguntas, limpa histórico desta categoria para
            // reiniciar o ciclo!
            if (catId != null) {
                userQuestionHistoryRepository.deleteByUserIdAndQuestion_Category_Id(userId, catId);
                questions = userQuestionHistoryRepository.findUnseenByCategory(userId, catId, PageRequest.of(0, 5));
            }
            if (questions.size() < 5) {
                // Fallback final: perguntas aleatórias do banco
                List<String> catNames = category != null ? List.of(category.getName())
                        : List.of("math", "history", "science", "geography");
                questions = questionRepository.findRandomQuestions(catNames, difficulty.name());
            }
        }

        if (questions.size() > 5) {
            questions = questions.subList(0, 5);
        }

        // Converter para QuestionResponse
        List<QuestionResponse> qResponses = questions.stream()
                .map(this::convertToQuestionResponse)
                .collect(Collectors.toList());

        // 2. Configurar Bot Oponente
        SoloStartLevelResponse response = new SoloStartLevelResponse();
        response.setLevelNumber(levelNumber);
        response.setCategoryName(category != null ? category.getName() : "mixed");
        response.setDifficulty(difficulty.name());
        response.setIsBossLevel(progress.getIsBossLevel());
        response.setQuestions(qResponses);

        if (progress.getIsBossLevel()) {
            response.setBotName(getBossName(levelNumber));
            response.setBotAvatar(getBossAvatar(levelNumber));
            response.setBotAccuracyRate(0.90); // Boss acerta 90%
            response.setBotMinDelayMs(3000);
            response.setBotMaxDelayMs(5500);
            response.setBossTaunt(getBossTaunt(levelNumber));
        } else {
            response.setBotName(getBotName(levelNumber));
            response.setBotAvatar("bot_avatar_" + (levelNumber % 5 + 1));
            // Dificuldade gradual dos BOTs comuns
            double accuracy = 0.60 + (levelNumber * 0.015);
            response.setBotAccuracyRate(Math.min(accuracy, 0.85));
            response.setBotMinDelayMs(4000);
            response.setBotMaxDelayMs(7500);
            response.setBossTaunt(null);
        }

        return response;
    }

    @Transactional
    public SoloFinishLevelResponse finishLevel(SoloFinishLevelRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        Integer levelNumber = request.getLevelNumber();
        SoloLevelProgress progress = soloLevelProgressRepository
                .findByUserIdAndLevelNumber(user.getId(), levelNumber)
                .orElseThrow(() -> new RuntimeException("Nível não encontrado"));

        // Registar perguntas no UserQuestionHistory
        if (request.getAnsweredQuestions() != null) {
            for (SoloFinishLevelRequest.QuestionAnswerDto dto : request.getAnsweredQuestions()) {
                Question q = questionRepository.findById(dto.getQuestionId()).orElse(null);
                if (q != null) {
                    Optional<UserQuestionHistory> existing = userQuestionHistoryRepository
                            .findByUserIdAndQuestionId(user.getId(), q.getId());
                    if (existing.isPresent()) {
                        UserQuestionHistory hist = existing.get();
                        hist.setAnsweredAt(LocalDateTime.now());
                        hist.setWasCorrect(dto.getWasCorrect());
                        if (dto.getWasCorrect()) {
                            hist.setConsecutiveCorrect(hist.getConsecutiveCorrect() + 1);
                        } else {
                            hist.setConsecutiveCorrect(0);
                        }
                        userQuestionHistoryRepository.save(hist);
                    } else {
                        userQuestionHistoryRepository.save(new UserQuestionHistory(user, q, dto.getWasCorrect()));
                    }
                }
            }
        }

        boolean victory = request.getPlayerScore() > request.getBotScore();
        SoloFinishLevelResponse response = new SoloFinishLevelResponse();
        response.setLevelNumber(levelNumber);
        response.setPlayerScore(request.getPlayerScore());
        response.setBotScore(request.getBotScore());
        response.setIsBossLevel(progress.getIsBossLevel());
        response.setVictory(victory);

        if (victory) {
            boolean isFirstClear = !Boolean.TRUE.equals(progress.getCompleted());

            // Pontos de Temporada: 15 na primeira vitória, 5 em repetição/treino
            int seasonPts = isFirstClear ? 15 : 5;
            seasonService.addSeasonPoints(user.getId(), seasonPts);

            // Calcular Estrelas (3 estrelas se acertou 100%, 2 estrelas se 80%, 1 se
            // venceu)
            double accRate = (double) request.getCorrectCount() / Math.max(1, request.getTotalQuestions());
            int stars = 1;
            if (accRate >= 1.0)
                stars = 3;
            else if (accRate >= 0.75)
                stars = 2;

            response.setStarsEarned(stars);

            // Recompensas: 100% no First Clear, reduzido no Replay (treino)
            int xpEarned;
            int coinsEarned;
            if (isFirstClear) {
                xpEarned = progress.getIsBossLevel() ? 100 : 40;
                coinsEarned = progress.getIsBossLevel() ? 50 : 20;
            } else {
                // Replay / Treino
                xpEarned = progress.getIsBossLevel() ? 25 : 10;
                coinsEarned = progress.getIsBossLevel() ? 10 : 5;
            }

            response.setXpEarned(xpEarned);
            response.setCoinsEarned(coinsEarned);

            // Atualizar Progresso do Nível
            progress.setCompleted(true);
            if (stars > progress.getStarsCount()) {
                progress.setStarsCount(stars);
            }
            if (request.getPlayerScore() > progress.getHighScore()) {
                progress.setHighScore(request.getPlayerScore());
            }
            soloLevelProgressRepository.save(progress);

            // Atualizar economia do utilizador
            user.setXp(user.getXp() + xpEarned);
            user.setCoins(user.getCoins() + coinsEarned);

            // Desbloquear Nível Seguinte
            if (levelNumber < TOTAL_MAP_LEVELS) {
                SoloLevelProgress nextLevel = soloLevelProgressRepository
                        .findByUserIdAndLevelNumber(user.getId(), levelNumber + 1)
                        .orElse(null);
                
                int requiredStars = 0;
                if ((levelNumber + 1) > 1 && levelNumber % 5 == 0) {
                    requiredStars = levelNumber * 2;
                }
                
                int newTotalStars = soloLevelProgressRepository.findByUserIdOrderByLevelNumberAsc(user.getId())
                                        .stream().mapToInt(SoloLevelProgress::getStarsCount).sum();

                if (nextLevel != null && !nextLevel.getUnlocked() && newTotalStars >= requiredStars) {
                    nextLevel.setUnlocked(true);
                    soloLevelProgressRepository.save(nextLevel);
                }
            }

            // Se era Boss, marcar como derrotado
            if (progress.getIsBossLevel()) {
                SoloBossProgress bossProg = soloBossProgressRepository
                        .findByUserIdAndBossLevelNumber(user.getId(), levelNumber)
                        .orElseGet(() -> new SoloBossProgress(user, levelNumber));
                bossProg.setDefeated(true);
                bossProg.setLivesRemaining(3); // Reset de vidas ao vencer
                soloBossProgressRepository.save(bossProg);
            }

            userRepository.save(user);
            response.setCheckpointReverted(false);
            response.setNewCurrentLevel(Math.min(TOTAL_MAP_LEVELS, levelNumber + 1));
            response.setMessage("Vitória espetacular! Desbloqueaste o nível seguinte.");

        } else {
            // Derrota
            response.setStarsEarned(0);
            response.setXpEarned(10);
            response.setCoinsEarned(0);

            if (progress.getIsBossLevel()) {
                SoloBossProgress bossProg = soloBossProgressRepository
                        .findByUserIdAndBossLevelNumber(user.getId(), levelNumber)
                        .orElseGet(() -> soloBossProgressRepository.save(new SoloBossProgress(user, levelNumber)));

                int remaining = Math.max(0, bossProg.getLivesRemaining() - 1);
                bossProg.setLivesRemaining(remaining);
                bossProg.setLastAttemptAt(LocalDateTime.now());
                bossProg.setAttemptsCount(bossProg.getAttemptsCount() + 1);

                if (remaining == 0) {
                    // GAME OVER NO BOSS -> Retrocesso de Checkpoint!
                    bossProg.setLivesRemaining(3); // Reset de vidas para quando voltar
                    soloBossProgressRepository.save(bossProg);

                    // Trancar os níveis anteriores (retroceder 3 níveis)
                    int revertTargetLevel = Math.max(1, levelNumber - 3);
                    for (int i = revertTargetLevel + 1; i <= levelNumber; i++) {
                        SoloLevelProgress p = soloLevelProgressRepository.findByUserIdAndLevelNumber(user.getId(), i)
                                .orElse(null);
                        if (p != null) {
                            p.setUnlocked(false);
                            soloLevelProgressRepository.save(p);
                        }
                    }

                    response.setCheckpointReverted(true);
                    response.setNewCurrentLevel(revertTargetLevel);
                    response.setBossLivesRemaining(0);
                    response.setMessage("Perdeste todas as vidas contra o Boss! Foste retrocedido para o Nível "
                            + revertTargetLevel);
                } else {
                    soloBossProgressRepository.save(bossProg);
                    response.setCheckpointReverted(false);
                    response.setNewCurrentLevel(levelNumber);
                    response.setBossLivesRemaining(remaining);
                    response.setMessage("Derrota! Restam-te " + remaining + " vidas contra o Boss.");
                }
            } else {
                response.setCheckpointReverted(false);
                response.setNewCurrentLevel(levelNumber);
                response.setBossLivesRemaining(3);
                response.setMessage("O BOT fez mais pontos. Tenta novamente!");
            }
        }

        return response;
    }

    // Helper methods para regras do Mapa
    private List<SoloLevelProgress> initializeMapForUser(User user) {
        List<SoloLevelProgress> list = new ArrayList<>();
        for (int i = 1; i <= TOTAL_MAP_LEVELS; i++) {
            boolean unlocked = (i == 1);
            boolean isBoss = (i % 5 == 0);
            SoloLevelProgress p = new SoloLevelProgress(user, i, unlocked, isBoss);
            list.add(soloLevelProgressRepository.save(p));
            if (isBoss) {
                soloBossProgressRepository.save(new SoloBossProgress(user, i));
            }
        }
        return list;
    }

    private Category getCategoryForLevel(int level) {
        List<Category> allCats = categoryRepository.findAll();
        if (allCats.isEmpty())
            return null;

        // Atribuição de categoria por blocos de 5 níveis
        int index = ((level - 1) / 5) % allCats.size();
        return allCats.get(index);
    }

    private Difficulty getDifficultyForLevel(int level) {
        int posInChapter = (level - 1) % 5;
        if (posInChapter <= 1)
            return Difficulty.EASY;
        if (posInChapter <= 3)
            return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }

    private String getBotName(int level) {
        String[] names = { "Sofia Bot", "Lucas AI", "Pedro Quiz", "Ana Mente", "Hugo Duelo", "Clara Saber",
                "Tiago Trivia" };
        return names[level % names.length];
    }

    private String getBossName(int level) {
        switch (level) {
            case 5:
                return "Dr. Histórico";
            case 10:
                return "Dra. Atômica";
            case 15:
                return "Capitão Globo";
            case 20:
                return "Mestre Supremo Krahn";
            default:
                return "Boss Supremo";
        }
    }

    private String getBossAvatar(int level) {
        switch (level) {
            case 5:
                return "boss_history";
            case 10:
                return "boss_science";
            case 15:
                return "boss_geography";
            case 20:
                return "boss_supreme";
            default:
                return "boss_default";
        }
    }

    private String getBossTaunt(int level) {
        switch (level) {
            case 5:
                return "Julgas que conheces o passado? Eu vivi a história!";
            case 10:
                return "A ciência não perdoa erros. Prepara-te para a derrota!";
            case 15:
                return "Nenhum mapa te salvará do meu conhecimento!";
            case 20:
                return "Chegaste ao fim da linha. Mostra do que és capaz!";
            default:
                return "Desafio-te para o duelo final!";
        }
    }

    private QuestionResponse convertToQuestionResponse(Question q) {
        QuestionResponse resp = new QuestionResponse();
        resp.setId(q.getId());
        resp.setQuestionText(q.getQuestionText());
        resp.setOptions(q.getOptions());
        resp.setCorrectAnswer(q.getCorrectAnswer());
        if (q.getCategory() != null) {
            quizmaster.quiz.models.Category cat = new quizmaster.quiz.models.Category();
            cat.setId(q.getCategory().getId());
            cat.setName(q.getCategory().getName());
            cat.setDisplayName(q.getCategory().getDisplayName());
            resp.setCategory(cat);
        } else {
            resp.setCategory(null);
        }
        resp.setDifficulty(q.getDifficulty() != null ? q.getDifficulty() : Difficulty.MEDIUM);
        resp.setPoints(q.getPoints() != null ? q.getPoints() : 100);
        return resp;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getFreeModeQuestions(List<Long> seenIds, int limit) {
        List<Question> questions;
        if (seenIds == null || seenIds.isEmpty()) {
            questions = questionRepository.findRandomQuestionsWithLimit(PageRequest.of(0, limit));
        } else {
            questions = questionRepository.findRandomUnseenQuestions(seenIds, PageRequest.of(0, limit));
            if (questions.isEmpty()) {
                // If all questions are seen, fallback to any random question
                questions = questionRepository.findRandomQuestionsWithLimit(PageRequest.of(0, limit));
            }
        }

        return questions.stream()
                .map(this::convertToQuestionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FreeModeScoreResponse saveFreeModeScore(Long userId, FreeModeScoreRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado: " + userId));

        // Calcular e atribuir recompensas (Modo Livre)
        int xpGained = request.getScore() / 20;
        int coinsEarned = request.getScore() / 100;

        user.setXp((user.getXp() != null ? user.getXp() : 0) + xpGained);
        user.setCoins((user.getCoins() != null ? user.getCoins() : 0) + coinsEarned);
        user.updateLevelBasedOnXp();
        userRepository.save(user);

        Optional<FreeModeScore> existingOpt = freeModeScoreRepository.findByUserAndGameMode(user, request.getGameMode());
        FreeModeScore existingScore;
        boolean isNewRecord = false;
        int personalBest = request.getScore();

        if (existingOpt.isPresent()) {
            existingScore = existingOpt.get();
            if (request.getScore() > existingScore.getScore()) {
                existingScore.setScore(request.getScore());
                existingScore.setHighestStreak(Math.max(existingScore.getHighestStreak(), request.getStreak()));
                existingScore.setCreatedAt(LocalDateTime.now());
                isNewRecord = true;
            } else {
                personalBest = existingScore.getScore();
                if (request.getStreak() > existingScore.getHighestStreak()) {
                    existingScore.setHighestStreak(request.getStreak());
                }
            }
        } else {
            existingScore = new FreeModeScore(user, request.getGameMode(), request.getScore(), request.getStreak());
            isNewRecord = true;
        }

        freeModeScoreRepository.save(existingScore);

        Optional<FreeModeScore> nextPlayerOpt = freeModeScoreRepository.findFirstByGameModeAndScoreGreaterThanOrderByScoreAsc(request.getGameMode(), personalBest);

        FreeModeScoreResponse response = new FreeModeScoreResponse();
        response.setNewRecord(isNewRecord);
        response.setPersonalBest(personalBest);
        response.setCoinsEarned(coinsEarned);
        response.setXpGained(xpGained);

        if (nextPlayerOpt.isPresent()) {
            FreeModeScore nextPlayer = nextPlayerOpt.get();
            response.setNextPlayerName(nextPlayer.getUser().getUsername());
            response.setNextPlayerScore(nextPlayer.getScore());
        }

        return response;
    }

    public List<LeaderboardEntryDto> getFreeModeLeaderboard(String gameMode) {
        List<FreeModeScore> topScores = freeModeScoreRepository.findTop100ByGameModeOrderByScoreDesc(gameMode);
        return topScores.stream()
                .map(score -> new LeaderboardEntryDto(score.getUser().getUsername(), score.getScore(), score.getHighestStreak()))
                .collect(Collectors.toList());
    }
}
