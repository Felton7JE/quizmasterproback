package quizmaster.quiz.controller;

import quizmaster.quiz.dto.*;
import quizmaster.quiz.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameController {
    
    private final GameService gameService;
    
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long gameId) {
        GameResponse game = gameService.getGame(gameId);
        return ResponseEntity.ok(game);
    }
    
    @GetMapping("/{gameId}/questions")
    public ResponseEntity<List<QuestionResponse>> getGameQuestions(
            @PathVariable Long gameId,
            @RequestParam Long userId) {
        try {
            List<QuestionResponse> questions = gameService.getGameQuestions(gameId, userId);
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    @GetMapping("/{gameId}/questions/{questionIndex}")
    public ResponseEntity<QuestionResponse> getCurrentQuestion(
            @PathVariable Long gameId,
            @PathVariable Integer questionIndex) {
        QuestionResponse question = gameService.getCurrentQuestion(gameId, questionIndex);
        return ResponseEntity.ok(question);
    }
    
    @PostMapping("/{gameId}/answers")
    public ResponseEntity<AnswerResponse> submitAnswer(
            @PathVariable Long gameId, 
            @Valid @RequestBody SubmitAnswerRequest request) {
        AnswerResponse answer = gameService.submitAnswer(gameId, request);
        return ResponseEntity.ok(answer);
    }
    
    @GetMapping("/{gameId}/results")
    public ResponseEntity<GameResultResponse> getGameResults(@PathVariable Long gameId) {
        GameResultResponse results = gameService.getGameResults(gameId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/{gameId}/leaderboard")
    public ResponseEntity<List<PlayerResultResponse>> getLeaderboard(@PathVariable Long gameId) {
        List<PlayerResultResponse> leaderboard = gameService.getLeaderboard(gameId);
        return ResponseEntity.ok(leaderboard);
    }
    
    @GetMapping("/{gameId}/leaderboard/live")
    public ResponseEntity<List<PlayerResultResponse>> getLiveLeaderboard(@PathVariable Long gameId) {
        List<PlayerResultResponse> leaderboard = gameService.getLiveLeaderboard(gameId);
        return ResponseEntity.ok(leaderboard);
    }
    
    @PostMapping("/{gameId}/pause")
    public ResponseEntity<Void> pauseGame(@PathVariable Long gameId, @RequestParam Long hostId) {
        gameService.pauseGame(gameId, hostId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{gameId}/resume")
    public ResponseEntity<Void> resumeGame(@PathVariable Long gameId, @RequestParam Long hostId) {
        gameService.resumeGame(gameId, hostId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{gameId}/finish")
    public ResponseEntity<GameResultResponse> finishGame(@PathVariable Long gameId, @RequestParam Long hostId) {
        GameResultResponse results = gameService.finishGame(gameId, hostId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/{gameId}/stats")
    public ResponseEntity<GameStatsResponse> getGameStats(@PathVariable Long gameId) {
        GameStatsResponse stats = gameService.getGameStats(gameId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/{gameId}/players/{userId}/answers")
    public ResponseEntity<List<PlayerAnswerResponse>> getPlayerAnswers(
            @PathVariable Long gameId,
            @PathVariable Long userId) {
        List<PlayerAnswerResponse> answers = gameService.getPlayerAnswers(gameId, userId);
        return ResponseEntity.ok(answers);
    }
}
