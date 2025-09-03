package quizmaster.quiz.controller;

import quizmaster.quiz.dto.UserResponse;
import quizmaster.quiz.service.AuthService;
import quizmaster.quiz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestParam String username) {
        var user = authService.authenticate(username);
        var userResponse = userService.getUser(user.getId());
        return ResponseEntity.ok(userResponse);
    }
    
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        return ResponseEntity.ok(available);
    }
}
