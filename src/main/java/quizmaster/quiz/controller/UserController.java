package quizmaster.quiz.controller;

import quizmaster.quiz.dto.*;
import quizmaster.quiz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@PathVariable Long userId) {
        UserStatsResponse stats = userService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingResponse>> getRanking(
            @RequestParam(defaultValue = "global") String period,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<RankingResponse> ranking = userService.getRanking(period, category, page, size);
        return ResponseEntity.ok(ranking);
    }
    
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<GameResultResponse>> getUserGameHistory(@PathVariable Long userId) {
        List<GameResultResponse> history = userService.getUserGameHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        List<UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
}
