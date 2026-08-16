package quizmaster.quiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.models.StoreItem;
import quizmaster.quiz.models.User;
import quizmaster.quiz.models.UserItem;
import quizmaster.quiz.models.UserMission;
import quizmaster.quiz.repository.StoreItemRepository;
import quizmaster.quiz.repository.UserItemRepository;
import quizmaster.quiz.repository.UserMissionRepository;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.services.GamificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMissionRepository userMissionRepository;

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private StoreItemRepository storeItemRepository;

    @GetMapping("/active")
    public ResponseEntity<?> getActiveMissions(@RequestHeader("Authorization") String token) {
        Long userId = 1L;
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String userIdStr = token.replace("Bearer ", "").trim();
                userId = Long.parseLong(userIdStr);
            }
        } catch (NumberFormatException e) {
            // fallback to 1L
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        List<UserMission> missions = gamificationService.getOrGenerateMissions(user);

        // Map to DTO
        List<Map<String, Object>> response = missions.stream().map(um -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", um.getId());
            map.put("title", "Missão de " + um.getMission().getActionType());
            map.put("description", um.getMission().getDescription());
            map.put("targetValue", um.getMission().getTargetValue());
            map.put("currentValue", um.getCurrentValue());
            map.put("rewardCoins", um.getMission().getRewardCoins());
            map.put("isCompleted", um.getIsCompleted());
            map.put("rewardClaimed", um.getRewardClaimed());
            map.put("type", um.getMission().getType().name());
            map.put("rewardItemId", um.getMission().getRewardItemId());
            map.put("rewardItemType", um.getMission().getRewardItemType());
            map.put("rewardItemName", um.getMission().getRewardItemName());
            map.put("rewardItemValue", um.getMission().getRewardItemValue());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{missionId}/claim")
    public ResponseEntity<?> claimReward(@PathVariable Long missionId, @RequestHeader("Authorization") String token) {
        Long userId = 1L;
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String userIdStr = token.replace("Bearer ", "").trim();
                userId = Long.parseLong(userIdStr);
            }
        } catch (NumberFormatException e) {
            // fallback to 1L
        }

        UserMission um = userMissionRepository.findById(missionId).orElse(null);
        if (um == null) {
            return ResponseEntity.badRequest().body("Mission not found");
        }
        
        // Ensure the mission belongs to the current user
        if (!um.getUser().getId().equals(userId)) {
            return ResponseEntity.badRequest().body("Unauthorized mission claim");
        }

        if (!um.getIsCompleted()) {
            return ResponseEntity.badRequest().body("Mission not completed yet");
        }

        if (um.getRewardClaimed()) {
            return ResponseEntity.badRequest().body("Reward already claimed");
        }

        um.setRewardClaimed(true);
        userMissionRepository.save(um);

        User user = um.getUser();
        user.setCoins(user.getCoins() + um.getMission().getRewardCoins());
        userRepository.save(user);

        // Grant cosmetic item reward if configured
        String rewardItemName = um.getMission().getRewardItemName();
        String rewardItemValue = um.getMission().getRewardItemValue();
        Long rewardItemId = um.getMission().getRewardItemId();

        if (rewardItemId != null || rewardItemValue != null) {
            Optional<StoreItem> storeItemOpt = Optional.empty();
            if (rewardItemId != null) {
                storeItemOpt = storeItemRepository.findById(rewardItemId);
            }
            if (storeItemOpt.isEmpty() && rewardItemValue != null) {
                storeItemOpt = storeItemRepository.findFirstByValue(rewardItemValue);
            }
            
            if (storeItemOpt.isPresent()) {
                StoreItem item = storeItemOpt.get();
                if (!userItemRepository.existsByUserAndStoreItem_Id(user, item.getId())) {
                    UserItem ui = new UserItem();
                    ui.setUser(user);
                    ui.setStoreItem(item);
                    ui.setIsEquipped(false);
                    userItemRepository.save(ui);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newCoinsBalance", user.getCoins());
        response.put("rewardItemName", rewardItemName);
        response.put("rewardItemValue", rewardItemValue);
        response.put("rewardItemType", um.getMission().getRewardItemType());
        
        return ResponseEntity.ok(response);
    }
}
