package quizmaster.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.dto.*;
import quizmaster.quiz.service.SoloService;

@RestController
@RequestMapping("/api/solo")
@RequiredArgsConstructor
public class SoloController {

    private final SoloService soloService;

    @GetMapping("/map/{userId}")
    public ResponseEntity<SoloMapResponse> getMapProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(soloService.getMapProgress(userId));
    }

    @GetMapping("/level/{levelNumber}/start")
    public ResponseEntity<SoloStartLevelResponse> startLevel(
            @PathVariable Integer levelNumber,
            @RequestParam Long userId) {
        return ResponseEntity.ok(soloService.startLevel(userId, levelNumber));
    }

    @PostMapping("/level/finish")
    public ResponseEntity<SoloFinishLevelResponse> finishLevel(@RequestBody SoloFinishLevelRequest request) {
        return ResponseEntity.ok(soloService.finishLevel(request));
    }

    @PostMapping("/free-mode/questions")
    public ResponseEntity<java.util.List<QuestionResponse>> getFreeModeQuestions(
            @RequestBody(required = false) java.util.List<Long> seenIds,
            @RequestParam(defaultValue = "10") int limit) {
        if (seenIds == null) {
            seenIds = new java.util.ArrayList<>();
        }
        return ResponseEntity.ok(soloService.getFreeModeQuestions(seenIds, limit));
    }

    @PostMapping("/free-mode/score")
    public ResponseEntity<FreeModeScoreResponse> saveFreeModeScore(
            @RequestParam Long userId,
            @RequestBody FreeModeScoreRequest request) {
        return ResponseEntity.ok(soloService.saveFreeModeScore(userId, request));
    }

    @GetMapping("/free-mode/leaderboard/{gameMode}")
    public ResponseEntity<java.util.List<LeaderboardEntryDto>> getLeaderboard(
            @PathVariable String gameMode) {
        return ResponseEntity.ok(soloService.getFreeModeLeaderboard(gameMode));
    }
}
