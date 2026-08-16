package quizmaster.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.dto.ActivityLogResponse;
import quizmaster.quiz.dto.GlobalNotificationRequest;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.service.ActivityService;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    @GetMapping("/feed/{userId}")
    public ResponseEntity<List<ActivityLogResponse>> getUserFeed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
                
        return ResponseEntity.ok(activityService.getFeedForUser(user, page, size));
    }

    @PostMapping("/global")
    public ResponseEntity<Void> createGlobalNotification(@RequestBody GlobalNotificationRequest request) {
        // Num ambiente real, verificar se o chamador é um ADMIN
        activityService.logGlobalNotification(request.getTitle(), request.getDescription());
        return ResponseEntity.ok().build();
    }
}
