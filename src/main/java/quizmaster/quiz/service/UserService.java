package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import quizmaster.quiz.dto.CreateUserRequest;
import quizmaster.quiz.dto.GameResultResponse;
import quizmaster.quiz.dto.RankingResponse;
import quizmaster.quiz.dto.UserResponse;
import quizmaster.quiz.dto.UserStatsResponse;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.GameResultRepository;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.repository.TitleRepository;
import quizmaster.quiz.repository.StoreItemRepository;
import quizmaster.quiz.repository.FreeModeScoreRepository;
import quizmaster.quiz.repository.SeasonRepository;
import quizmaster.quiz.repository.UserSeasonProgressRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final quizmaster.quiz.repository.UserCategoryStatsRepository userCategoryStatsRepository;
    
    @Autowired
    private TitleRepository titleRepository;
    
    @Autowired
    private StoreItemRepository storeItemRepository;
    
    @Autowired
    private FreeModeScoreRepository freeModeScoreRepository;
    
    @Autowired
    private SeasonRepository seasonRepository;
    
    @Autowired
    private UserSeasonProgressRepository userSeasonProgressRepository;
    
    private final GameResultRepository gameResultRepository;
    
    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        user.setCreatedAt(LocalDateTime.now());
        
        String code = generateReferralCode();
        while (userRepository.existsByReferralCode(code)) {
            code = generateReferralCode();
        }
        user.setReferralCode(code);
        
        user = userRepository.save(user);
        return convertToUserResponse(user);
    }
    
    private String generateReferralCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("QM-");
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!user.isActive()) {
            throw new RuntimeException("Conta desativada.");
        }
        return convertToUserResponse(user);
    }
    
    public UserStatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!user.isActive()) {
            throw new RuntimeException("Conta desativada.");
        }
        
        UserStatsResponse stats = new UserStatsResponse();
        stats.setUserId(user.getId());
        stats.setUsername(user.getUsername());
        stats.setTotalPoints(user.getTotalPoints());
        stats.setGamesPlayed(user.getGamesPlayed());
        stats.setGamesWon(user.getGamesWon());
        stats.setAccuracy(user.getAccuracy());
        stats.setBestStreak(user.getBestStreak());
        
        // Calcular win rate
        if (user.getGamesPlayed() > 0) {
            stats.setWinRate((double) user.getGamesWon() / user.getGamesPlayed() * 100);
        } else {
            stats.setWinRate(0.0);
        }
        
        // --- Free Mode Stats ---
        freeModeScoreRepository.findByUserAndGameMode(user, "SURVIVAL").ifPresent(score -> {
            stats.setSurvivalHighScore(score.getScore());
            stats.setSurvivalBestStreak(score.getHighestStreak());
        });
        
        freeModeScoreRepository.findByUserAndGameMode(user, "TIME_ATTACK").ifPresent(score -> {
            stats.setTimeAttackHighScore(score.getScore());
        });
        
        // --- Season Stats ---
        seasonRepository.findFirstByActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime.now(), LocalDateTime.now())
            .flatMap(activeSeason -> userSeasonProgressRepository.findByUserIdAndSeasonId(userId, activeSeason.getId()))
            .ifPresent(progress -> {
                stats.setSeasonLevel(progress.getCurrentLevel());
                stats.setSeasonPoints(progress.getSeasonPoints());
                stats.setHasPremiumPass(progress.isPremiumPass());
            });
        
        // Calcular rankings
        List<User> allUsers = userRepository.findAllOrderByTotalPointsDesc();
        stats.setGlobalRanking(IntStream.range(0, allUsers.size())
                .filter(i -> allUsers.get(i).getId().equals(userId))
                .findFirst()
                .orElse(-1) + 1);
        
        return stats;
    }
    
    public List<RankingResponse> getRanking(String period, String category) {
        boolean isWeekly = "weekly".equalsIgnoreCase(period);
        boolean isMonthly = "monthly".equalsIgnoreCase(period);

        if (category == null || category.isEmpty() || category.equalsIgnoreCase("all") || category.equalsIgnoreCase("global")) {
            List<User> users;
            if (isWeekly) {
                users = userRepository.findAllOrderByWeeklyPointsDesc();
            } else if (isMonthly) {
                users = userRepository.findAllOrderByMonthlyPointsDesc();
            } else {
                users = userRepository.findAllOrderByTotalPointsDesc();
            }

            return IntStream.range(0, Math.min(users.size(), 100))
                    .mapToObj(i -> {
                        User user = users.get(i);
                        Integer points = isWeekly ? user.getWeeklyPoints() : (isMonthly ? user.getMonthlyPoints() : user.getTotalPoints());
                        return buildRankingResponse(i + 1, user, points != null ? points : 0);
                    })
                    .collect(Collectors.toList());
        } else {
            List<quizmaster.quiz.models.UserCategoryStats> stats;
            if (isWeekly) {
                stats = userCategoryStatsRepository.findTopWeeklyByCategoryName(category.toUpperCase());
            } else if (isMonthly) {
                stats = userCategoryStatsRepository.findTopMonthlyByCategoryName(category.toUpperCase());
            } else {
                stats = userCategoryStatsRepository.findTopByCategoryName(category.toUpperCase());
            }

            return IntStream.range(0, Math.min(stats.size(), 100))
                    .mapToObj(i -> {
                        quizmaster.quiz.models.UserCategoryStats stat = stats.get(i);
                        Integer points = isWeekly ? stat.getWeeklyPoints() : (isMonthly ? stat.getMonthlyPoints() : stat.getTotalPoints());
                        return buildRankingResponse(i + 1, stat.getUser(), points != null ? points : 0);
                    })
                    .collect(Collectors.toList());
        }
    }
    
    private RankingResponse buildRankingResponse(int position, User user, Integer points) {
        RankingResponse ranking = new RankingResponse();
        ranking.setPosition(position);
        ranking.setUserId(user.getId());
        ranking.setUsername(user.getUsername());
        ranking.setAvatar(user.getAvatar());
        ranking.setTotalPoints(points);
        ranking.setGamesPlayed(user.getGamesPlayed());
        ranking.setGamesWon(user.getGamesWon());
        ranking.setAccuracy(user.getAccuracy());
        ranking.setActiveTitleId(user.getActiveTitleId());
        ranking.setActiveBannerId(user.getActiveBannerId());
        ranking.setActivePhraseId(user.getActivePhraseId());
        
        if (user.getActiveTitleId() != null) {
            titleRepository.findById(user.getActiveTitleId()).ifPresent(title -> {
                ranking.setActiveTitleName(title.getName());
            });
        }
        if (user.getActiveBannerId() != null) {
            storeItemRepository.findById(user.getActiveBannerId()).ifPresent(item -> {
                ranking.setActiveBannerUrl(item.getValue());
            });
        }
        
        if (user.getGamesPlayed() != null && user.getGamesPlayed() > 0) {
            ranking.setWinRate((double) (user.getGamesWon() != null ? user.getGamesWon() : 0) / user.getGamesPlayed() * 100);
        } else {
            ranking.setWinRate(0.0);
        }
        
        ranking.setStreak(user.getCurrentStreak() != null ? user.getCurrentStreak() : 0);
        ranking.setLevel(user.getLevel() != null ? user.getLevel() : 1);
        ranking.setIsVip(userSeasonProgressRepository.existsByUserIdAndSeason_ActiveTrueAndIsPremiumPassTrue(user.getId()));
        
        return ranking;
    }
    
    public List<RankingResponse> getRanking(String period, String category, int page, int size) {
        // TODO: Implement pagination and filtering
        return getRanking(period, category);
    }
    
    public UserResponse updateUser(Long userId, CreateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        
        user = userRepository.save(user);
        return convertToUserResponse(user);
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }
    
    public List<GameResultResponse> getUserGameHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<quizmaster.quiz.models.GameResult> userResults = gameResultRepository.findByUserOrderByGame_StartedAtDesc(user);

        return userResults.stream().map(ur -> {
            quizmaster.quiz.models.Game game = ur.getGame();
            List<quizmaster.quiz.models.GameResult> allResults = gameResultRepository.findByGameOrderByTotalPointsDesc(game);

            GameResultResponse response = new GameResultResponse();
            response.setGameId(game.getId());
            response.setGameMode(game.getRoom() != null && game.getRoom().getGameMode() != null ? game.getRoom().getGameMode().name() : "CLASSIC");
            response.setStartedAt(game.getStartedAt());
            response.setEndedAt(game.getEndedAt());

            List<quizmaster.quiz.dto.PlayerResultResponse> playerResults = allResults.stream()
                    .map(this::convertToPlayerResultResponse)
                    .collect(Collectors.toList());

            response.setResults(playerResults);
            if (!playerResults.isEmpty()) {
                response.setWinner(playerResults.get(0));
            }

            return response;
        }).collect(Collectors.toList());
    }
    
    private quizmaster.quiz.dto.PlayerResultResponse convertToPlayerResultResponse(quizmaster.quiz.models.GameResult result) {
        quizmaster.quiz.dto.PlayerResultResponse pr = new quizmaster.quiz.dto.PlayerResultResponse();
        pr.setUserId(result.getUser().getId());
        pr.setUsername(result.getUser().getUsername());
        pr.setAvatar(result.getUser().getAvatar());
        if (result.getTeam() != null) pr.setTeam(result.getTeam().name());
        pr.setCorrectAnswers(result.getCorrectAnswers());
        pr.setTotalQuestions(result.getTotalQuestions());
        pr.setTotalPoints(result.getTotalPoints());
        pr.setAccuracy(result.getAccuracy());
        pr.setBestStreak(result.getBestStreak());
        pr.setTotalTime(result.getTotalTime());
        pr.setPosition(result.getPosition());
        pr.setCoinsEarned(result.getCoinsEarned());
        pr.setXpEarned(result.getXpEarned());
        pr.setEloEarned(result.getEloEarned());
        if (result.getUser() != null) {
            pr.setActiveBannerId(result.getUser().getActiveBannerId());
            pr.setActiveAvatarId(result.getUser().getActiveAvatarId());
            pr.setActiveFrameId(result.getUser().getActiveFrameId());
            pr.setActivePhraseId(result.getUser().getActivePhraseId());
        }
        return pr;
    }
    
    public List<UserResponse> searchUsers(String query) {
        // TODO: Implement user search
        return List.of();
    }
    
    public UserResponse updateCrystals(Long userId, Integer crystals) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        user.setCrystals(crystals);
        userRepository.save(user);
        return convertToUserResponse(user);
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setTotalPoints(user.getTotalPoints());
        response.setGamesPlayed(user.getGamesPlayed());
        response.setGamesWon(user.getGamesWon());
        response.setAccuracy(user.getAccuracy());
        response.setBestStreak(user.getBestStreak());
        
        // Gamification & Economy
        response.setCoins(user.getCoins() != null ? user.getCoins() : 500);
        response.setCrystals(user.getCrystals() != null ? user.getCrystals() : 100);
        response.setEnergy(user.getEnergy());
        response.setXp(user.getXp());
        response.setLevel(user.getLevel());
        response.setActiveTitleId(user.getActiveTitleId());
        response.setActiveBannerId(user.getActiveBannerId());
        response.setActivePhraseId(user.getActivePhraseId());
        response.setActiveAvatarId(user.getActiveAvatarId());
        response.setActiveFrameId(user.getActiveFrameId());
        response.setActiveEmoteId(user.getActiveEmoteId());
        response.setCurrentLeague(user.getCurrentLeague());
        response.setEloPoints(user.getEloPoints());
        response.setIsVip(userSeasonProgressRepository.existsByUserIdAndSeason_ActiveTrueAndIsPremiumPassTrue(user.getId()));
        
        // Se a data do último quiz for anterior a hoje, a cota diária é 0
        java.time.LocalDate today = java.time.LocalDate.now();
        if (user.getLastAiQuizDate() == null || !user.getLastAiQuizDate().isEqual(today)) {
            response.setDailyAiQuizCount(0);
        } else {
            response.setDailyAiQuizCount(user.getDailyAiQuizCount() != null ? user.getDailyAiQuizCount() : 0);
        }
        response.setLastAiQuizTimestamp(user.getLastAiQuizTimestamp());
        
        // Garante que todo usuário possua um referralCode mesmo que criado anteriormente
        if (user.getReferralCode() == null || user.getReferralCode().isEmpty()) {
            String code = generateReferralCode();
            while (userRepository.existsByReferralCode(code)) {
                code = generateReferralCode();
            }
            user.setReferralCode(code);
            userRepository.save(user);
        }
        response.setReferralCode(user.getReferralCode());
        response.setReferralCount(user.getReferralCount() != null ? user.getReferralCount() : 0);
        
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public Map<String, Object> applyReferralCode(Long userId, String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("Código de convite não pode ser vazio.");
        }
        String cleanCode = code.trim().toUpperCase();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (user.getReferredById() != null) {
            throw new RuntimeException("Você já utilizou um código de convite anteriormente.");
        }

        User referrer = userRepository.findByReferralCode(cleanCode)
                .orElseThrow(() -> new RuntimeException("Código de convite '" + cleanCode + "' não encontrado."));

        if (referrer.getId().equals(user.getId())) {
            throw new RuntimeException("Não podes usar o teu próprio código de convite.");
        }

        // 1. Marca quem indicou e dá bónus de boas-vindas ao indicado (5 cristais)
        user.setReferredById(referrer.getId());
        user.setCrystals((user.getCrystals() != null ? user.getCrystals() : 0) + 5);
        userRepository.save(user);

        // 2. Incrementa contagem de indicados do padrinho
        int currentRefs = (referrer.getReferralCount() != null ? referrer.getReferralCount() : 0) + 1;
        referrer.setReferralCount(currentRefs);

        boolean bonusGranted = false;
        // Regra de ouro: Cada 5 amigos convidados = +15 Cristais 🔮 para o padrinho
        if (currentRefs % 5 == 0) {
            referrer.setCrystals((referrer.getCrystals() != null ? referrer.getCrystals() : 0) + 15);
            bonusGranted = true;
        }
        userRepository.save(referrer);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Código aplicado com sucesso! Recebeste 5 Cristais Mágicos 🔮 de boas-vindas!");
        result.put("referralCount", currentRefs);
        result.put("bonusGranted", bonusGranted);
        return result;
    }
}