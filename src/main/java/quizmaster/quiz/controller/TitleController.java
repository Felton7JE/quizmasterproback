package quizmaster.quiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.service.TitleService;
import quizmaster.quiz.dto.TitleDTO;
import quizmaster.quiz.dto.UserTitleDTO;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/titles")
public class TitleController {

    @Autowired
    private TitleService titleService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/available/{userId}")
    public ResponseEntity<List<TitleDTO>> getAllTitles(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(titleService.getAllTitles(user));
    }

    @GetMapping("/earned/{userId}")
    public ResponseEntity<List<UserTitleDTO>> getInventory(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(titleService.getUserTitles(user));
    }

    @PostMapping("/equip")
    public ResponseEntity<String> equipTitle(@RequestParam Long userId, @RequestParam Long titleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        titleService.equipTitle(user, titleId);
        return ResponseEntity.ok("Title equipped successfully");
    }

    @PostMapping("/unequip")
    public ResponseEntity<String> unequipTitle(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        titleService.unequipTitle(user);
        return ResponseEntity.ok("Title unequipped successfully");
    }
}
