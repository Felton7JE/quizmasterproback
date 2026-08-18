package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.dto.SeasonResponse;
import quizmaster.quiz.models.Season;
import quizmaster.quiz.models.SeasonReward;
import quizmaster.quiz.models.User;
import quizmaster.quiz.models.UserSeasonProgress;
import quizmaster.quiz.repository.SeasonRepository;
import quizmaster.quiz.repository.SeasonRewardRepository;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.repository.UserSeasonProgressRepository;
import quizmaster.quiz.repository.StoreItemRepository;
import quizmaster.quiz.repository.UserItemRepository;
import quizmaster.quiz.repository.TitleRepository;
import quizmaster.quiz.repository.UserTitleRepository;
import quizmaster.quiz.models.StoreItem;
import quizmaster.quiz.models.UserItem;
import quizmaster.quiz.models.Title;
import quizmaster.quiz.models.UserTitle;
import quizmaster.quiz.enums.RewardType;
import quizmaster.quiz.enums.ItemType;
import quizmaster.quiz.enums.TitleConditionType;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonRewardRepository seasonRewardRepository;
    private final UserSeasonProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final StoreItemRepository storeItemRepository;
    private final UserItemRepository userItemRepository;
    private final TitleRepository titleRepository;
    private final UserTitleRepository userTitleRepository;

    public Season getActiveSeason() {
        LocalDateTime now = LocalDateTime.now();
        return seasonRepository.findFirstByActiveTrueAndStartDateBeforeAndEndDateAfter(now, now).orElse(null);
    }

    @Transactional
    public SeasonResponse getSeasonData(Long userId) {
        Season activeSeason = getActiveSeason();
        if (activeSeason == null) {
            return null; // No active season
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        UserSeasonProgress progress = progressRepository.findByUserIdAndSeasonId(userId, activeSeason.getId())
                .orElseGet(() -> {
                    UserSeasonProgress newProgress = new UserSeasonProgress();
                    newProgress.setUser(user);
                    newProgress.setSeason(activeSeason);
                    return progressRepository.save(newProgress);
                });

        List<SeasonReward> rewards = seasonRewardRepository.findBySeasonIdOrderByLevelRequiredAsc(activeSeason.getId());

        return new SeasonResponse(
                activeSeason.getId(),
                activeSeason.getName(),
                activeSeason.getDescription(),
                activeSeason.getExclusiveCategoryId(),
                progress.getCurrentLevel(),
                progress.getSeasonPoints(),
                progress.isPremiumPass(),
                rewards,
                progress.getLastClaimedFreeLevel(),
                progress.getLastClaimedPremiumLevel(),
                activeSeason.getBannerUrl(),
                activeSeason.getMapBackgroundUrl(),
                activeSeason.getLockedNodeIconUrl(),
                activeSeason.getCurrentNodeIconUrl(),
                activeSeason.getCompletedNodeIconUrl()
        );
    }
    
    @Transactional
    public void addSeasonPoints(Long userId, int points) {
        Season activeSeason = getActiveSeason();
        if (activeSeason == null) return;
        
        progressRepository.findByUserIdAndSeasonId(userId, activeSeason.getId()).ifPresent(progress -> {
            progress.setSeasonPoints(progress.getSeasonPoints() + points);
            // Example logic: 100 points per level
            int newLevel = 1 + (progress.getSeasonPoints() / 100);
            if (newLevel > 30) newLevel = 30; // Max level 30
            progress.setCurrentLevel(newLevel);
            progressRepository.save(progress);
        });
    }
    @Transactional
    public void claimReward(Long userId, int level, boolean isPremium) {
        Season activeSeason = getActiveSeason();
        if (activeSeason == null) return;
        
        User user = userRepository.findById(userId).orElseThrow();
        UserSeasonProgress progress = progressRepository.findByUserIdAndSeasonId(userId, activeSeason.getId()).orElseThrow();
        
        if (level > progress.getCurrentLevel()) return; // Not reached yet
        
        if (isPremium) {
            if (!progress.isPremiumPass()) return;
            if (level <= progress.getLastClaimedPremiumLevel()) return;
            progress.setLastClaimedPremiumLevel(level);
        } else {
            if (level <= progress.getLastClaimedFreeLevel()) return;
            progress.setLastClaimedFreeLevel(level);
        }
        
        SeasonReward reward = seasonRewardRepository.findFirstBySeasonIdAndLevelRequired(activeSeason.getId(), level);
        if (reward != null) {
            RewardType type = isPremium ? reward.getPremiumRewardType() : reward.getFreeRewardType();
            String value = isPremium ? reward.getPremiumRewardValue() : reward.getFreeRewardValue();
            String imageUrl = isPremium ? reward.getPremiumRewardImageUrl() : reward.getFreeRewardImageUrl();
            
            if (RewardType.COIN == type && value != null) {
                user.setCoins((user.getCoins() != null ? user.getCoins() : 0) + Integer.parseInt(value));
            } else if (RewardType.ENERGY == type && value != null) {
                int energyToAdd = Integer.parseInt(value);
                user.setEnergy((user.getEnergy() != null ? user.getEnergy() : 0) + energyToAdd);
            } else if (RewardType.XP == type && value != null) {
                int xpToAdd = Integer.parseInt(value);
                user.setXp((user.getXp() != null ? user.getXp() : 0) + xpToAdd);
                user.updateLevelBasedOnXp();
            } else if (RewardType.AVATAR == type) {
                String avatarUrl = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : value;
                String avatarName = (value != null && !value.isEmpty()) ? value : "Avatar Nível " + level;
                
                StoreItem avatarItem = storeItemRepository.findFirstByValue(avatarUrl)
                        .orElseGet(() -> {
                            StoreItem item = new StoreItem();
                            item.setName(avatarName);
                            item.setDescription("Avatar exclusivo da Temporada " + activeSeason.getName());
                            item.setPrice(0);
                            item.setType(ItemType.AVATAR);
                            item.setValue(avatarUrl);
                            item.setRarity("Lendário");
                            return storeItemRepository.save(item);
                        });
                        
                if (!userItemRepository.existsByUserAndStoreItem_Id(user, avatarItem.getId())) {
                    UserItem userItem = new UserItem();
                    userItem.setUser(user);
                    userItem.setStoreItem(avatarItem);
                    userItem.setIsEquipped(false);
                    userItemRepository.save(userItem);
                }
            } else if (RewardType.TITLE == type && value != null && !value.isEmpty()) {
                Title title = titleRepository.findFirstByName(value)
                        .orElseGet(() -> {
                            Title t = new Title();
                            t.setName(value);
                            t.setDescription("Título exclusivo da Temporada " + activeSeason.getName());
                            t.setConditionType(TitleConditionType.LEVEL);
                            t.setConditionValue(level);
                            return titleRepository.save(t);
                        });
                        
                if (!userTitleRepository.existsByUserAndTitle_Id(user, title.getId())) {
                    UserTitle userTitle = new UserTitle();
                    userTitle.setUser(user);
                    userTitle.setTitle(title);
                    userTitle.setIsEquipped(false);
                    userTitleRepository.save(userTitle);
                }
            }
            userRepository.save(user);
        }
        
        progressRepository.save(progress);
    }

    @Transactional
    public SeasonResponse buyVipPass(Long userId) {
        Season activeSeason = getActiveSeason();
        if (activeSeason == null) {
            throw new RuntimeException("Nenhuma temporada ativa.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        int vipPrice = 50;
        if (user.getCrystals() == null || user.getCrystals() < vipPrice) {
            throw new RuntimeException("Cristais insuficientes para comprar o Passe VIP. (Custo: " + vipPrice + " cristais)");
        }

        user.setCrystals(user.getCrystals() - vipPrice);
        userRepository.save(user);

        UserSeasonProgress progress = progressRepository.findByUserIdAndSeasonId(userId, activeSeason.getId())
                .orElseGet(() -> {
                    UserSeasonProgress newProgress = new UserSeasonProgress();
                    newProgress.setUser(user);
                    newProgress.setSeason(activeSeason);
                    return progressRepository.save(newProgress);
                });

        progress.setPremiumPass(true);
        progressRepository.save(progress);

        return getSeasonData(userId);
    }
}
