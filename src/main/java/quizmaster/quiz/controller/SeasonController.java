package quizmaster.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.dto.SeasonResponse;
import quizmaster.quiz.service.SeasonService;

@RestController
@RequestMapping("/api/season")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    @GetMapping("/progress")
    public ResponseEntity<?> getSeasonProgress(@RequestParam Long userId) {
        SeasonResponse response = seasonService.getSeasonData(userId);
        if (response == null) {
            return ResponseEntity.ok().body("{\"active\": false}");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-points")
    public ResponseEntity<?> addPoints(@RequestParam Long userId, @RequestParam int points) {
        seasonService.addSeasonPoints(userId, points);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/claim")
    public ResponseEntity<?> claimReward(@RequestParam Long userId, @RequestParam int level, @RequestParam boolean isPremium) {
        seasonService.claimReward(userId, level, isPremium);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/buy-vip")
    public ResponseEntity<?> buyVipPass(@RequestParam Long userId) {
        try {
            SeasonResponse response = seasonService.buyVipPass(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
