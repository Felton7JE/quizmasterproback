package quizmaster.quiz.controller;

import quizmaster.quiz.dto.UserResponse;
import quizmaster.quiz.service.AuthService;
import quizmaster.quiz.service.UserService;
import quizmaster.quiz.auth.Seguranca.jwt.utilJwt;
import quizmaster.quiz.auth.DTO.GoogleAuthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    private final utilJwt jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestParam String username) {
        var user = authService.authenticate(username);
        var userResponse = userService.getUser(user.getId());
        userResponse.setToken(jwtUtil.gerarTokenPorUsername(user.getUsername()));
        return ResponseEntity.ok(userResponse);
    }
    
    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody GoogleAuthDTO googleAuthDTO) {
        try {
            var user = authService.authenticateGoogle(googleAuthDTO.getIdToken());
            var userResponse = userService.getUser(user.getId());
            userResponse.setToken(jwtUtil.gerarTokenPorUsername(user.getUsername()));
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falha ao autenticar com o Google.");
        }
    }
    
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        return ResponseEntity.ok(available);
    }
}
